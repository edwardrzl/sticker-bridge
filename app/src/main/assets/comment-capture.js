// Injected at document start into tiktok.com pages loaded by the hidden in-app browser.
// It observes the comment-list responses the page itself requests and forwards their text to
// the app through the "StickerBridge" message channel. It never makes requests of its own.
(function () {
  if (window.__stickerBridgeInstalled) return;
  window.__stickerBridgeInstalled = true;

  var COMMENT_LIST = '/api/comment/list/';

  function forward(text) {
    try {
      if (typeof text === 'string' && text.length > 0) StickerBridge.postMessage(text);
    } catch (e) {
      // The channel is missing on non-allowed origins; nothing to do.
    }
  }

  var originalFetch = window.fetch;
  if (typeof originalFetch === 'function') {
    window.fetch = function (input, init) {
      var url = typeof input === 'string' ? input : (input && input.url) || '';
      return originalFetch.apply(this, arguments).then(function (response) {
        if (url.indexOf(COMMENT_LIST) !== -1) {
          response.clone().text().then(forward).catch(function () {});
        }
        return response;
      });
    };
  }

  var originalOpen = XMLHttpRequest.prototype.open;
  XMLHttpRequest.prototype.open = function (method, url) {
    this.__stickerBridgeUrl = String(url || '');
    return originalOpen.apply(this, arguments);
  };

  var originalSend = XMLHttpRequest.prototype.send;
  XMLHttpRequest.prototype.send = function () {
    var xhr = this;
    if (xhr.__stickerBridgeUrl && xhr.__stickerBridgeUrl.indexOf(COMMENT_LIST) !== -1) {
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
})();
