// Injected at document start into tiktok.com pages loaded by the hidden in-app browser.
// It asks TikTok for the comment list, and for the replies of the comments the app names, from
// inside the page, and forwards the response text to the app through the "StickerBridge" message
// channel.
//
// Messages: "body:<response text>" for comment lists, "reply:<response text>" for one comment's
// replies, "replyfail:<comment id>" when those could not be read, "fail:<reason>" when the comment
// list gave up, and "diag:<note>" for diagnostics (API paths only, never response content or
// query strings).
(function () {
  if (window.__stickerBridgeInstalled) return;
  window.__stickerBridgeInstalled = true;

  var COMMENT_LIST = '/api/comment/list/';
  var REPLY_LIST = '/api/comment/list/reply/';

  // The reply list lives under the comment list's path, and its answers travel on their own.
  function isCommentList(url) {
    return url.indexOf(COMMENT_LIST) !== -1 && url.indexOf(REPLY_LIST) === -1;
  }
  var seenApiPaths = {};

  function send(message) {
    try {
      StickerBridge.postMessage(message);
    } catch (e) {
      // The channel is missing on non-allowed origins; nothing to do.
    }
  }

  function noteRequest(url) {
    try {
      var path = new URL(url, location.href).pathname;
      if (path.indexOf('/api/') !== -1 && !seenApiPaths[path]) {
        seenApiPaths[path] = true;
        send('diag:api ' + path);
      }
    } catch (e) {
      // Unparseable URL; ignore.
    }
  }

  function forward(text) {
    if (typeof text === 'string' && text.length > 0) send('body:' + text);
  }

  send('diag:script installed on ' + location.pathname);

  var originalFetch = window.fetch;
  if (typeof originalFetch === 'function') {
    window.fetch = function (input, init) {
      var url = typeof input === 'string' ? input : (input && input.url) || '';
      noteRequest(url);
      return originalFetch.apply(this, arguments).then(function (response) {
        if (isCommentList(url)) {
          response.clone().text().then(forward).catch(function () {});
        }
        return response;
      });
    };
  }

  var originalOpen = XMLHttpRequest.prototype.open;
  XMLHttpRequest.prototype.open = function (method, url) {
    this.__stickerBridgeUrl = String(url || '');
    noteRequest(this.__stickerBridgeUrl);
    return originalOpen.apply(this, arguments);
  };

  var originalSend = XMLHttpRequest.prototype.send;
  XMLHttpRequest.prototype.send = function () {
    var xhr = this;
    if (xhr.__stickerBridgeUrl && isCommentList(xhr.__stickerBridgeUrl)) {
      xhr.addEventListener('load', function () {
        try {
          forward(xhr.responseText);
        } catch (e) {
          // responseText is unavailable for binary response types.
        }
      });
    }
    return originalSend.apply(this, arguments);
  };

  // TikTok's desktop page no longer requests comments by itself, so ask for them from inside the
  // page, the way the page does when a person opens the comments. TikTok's own security code adds
  // the request signature; this script never computes one. The responses reach the app through the
  // fetch hook above.
  if (window.top !== window) return;

  // Asked as TikTok's Android app of a recent version: the website's own identity (aid=1988) gets
  // only the comments whose sticker type the website can show, about half of them (ADR-002).
  var CLIENT = 'aid=1233&device_platform=android&version_name=40.3.4';
  var INITIAL_PAGES = 3;
  var PAGE_SIZE = 20;
  var REPLY_PAGE_SIZE = 50;
  var MAX_RETRIES = 2;
  var RETRY_DELAY_MS = 3000;
  var START_DELAY_MS = 1000;

  // Where the next page starts, and whether TikTok has more comments.
  var nextCursor = 0;
  var hasMore = true;
  var requesting = false;

  function postId() {
    var match = location.pathname.match(/\/(video|photo)\/(\d+)/);
    return match ? match[2] : null;
  }

  /** Requests `pages` consecutive pages starting at `nextCursor`, retrying each one on failure. */
  function requestComments(pages, retries) {
    var id = postId();
    if (!id) {
      requesting = false;
      send('fail:no post id in ' + location.pathname);
      return;
    }
    requesting = true;
    var url = COMMENT_LIST + '?' + CLIENT + '&aweme_id=' + id + '&count=' + PAGE_SIZE + '&cursor=' + nextCursor;
    window
      .fetch(url, { credentials: 'include' })
      .then(function (response) {
        // The fetch hook resolves to nothing when the browser is closed mid-request.
        if (!response) throw new Error('no response');
        return response.json();
      })
      .then(function (body) {
        if (!body || body.status_code !== 0) throw new Error('status ' + (body && body.status_code));
        nextCursor = body.cursor;
        hasMore = !!body.has_more;
        if (hasMore && pages > 1) {
          requestComments(pages - 1, MAX_RETRIES);
        } else {
          requesting = false;
        }
      })
      .catch(function (error) {
        if (retries > 0) {
          send('diag:comment request failed (' + error.message + '), retrying');
          setTimeout(function () {
            requestComments(pages, retries - 1);
          }, RETRY_DELAY_MS);
        } else {
          requesting = false;
          send('fail:comment request failed (' + error.message + ')');
        }
      });
  }

  /** Called by the app for "load more": one more page, if TikTok has one. */
  window.__stickerBridgeLoadMore = function () {
    if (!requesting && hasMore) requestComments(1, MAX_RETRIES);
  };

  /** Called by the app with a comment id: reads the first page of that comment's replies (FR2.10). */
  window.__stickerBridgeLoadReplies = function (commentId) {
    var id = postId();
    if (!id || !/^[0-9]+$/.test(String(commentId))) {
      send('replyfail:' + commentId);
      return;
    }
    var url =
      REPLY_LIST + '?' + CLIENT + '&item_id=' + id + '&comment_id=' + commentId + '&count=' + REPLY_PAGE_SIZE + '&cursor=0';
    window
      .fetch(url, { credentials: 'include' })
      .then(function (response) {
        if (!response) throw new Error('no response');
        return response.text();
      })
      .then(function (text) {
        send('reply:' + text);
      })
      .catch(function () {
        send('replyfail:' + commentId);
      });
  };

  window.addEventListener('load', function () {
    setTimeout(function () {
      requestComments(INITIAL_PAGES, MAX_RETRIES);
    }, START_DELAY_MS);
  });
})();
