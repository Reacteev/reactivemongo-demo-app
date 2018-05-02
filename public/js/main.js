var wsUri = "wss://" + document.location.host + "/watchCollection";
var output;
var author;

function init() {
  author = window.prompt("Your name", localStorage.getItem("name") || "") || "";
  localStorage.setItem("name", author);
  output = document.getElementById("output");
  log = document.getElementById("log");
  testWebSocket();
  document.getElementById("message").addEventListener("submit", function(evt) {
    var content = document.getElementById("content");
    if (content && content.value && content.value.length > 0) {
      var doc = {
        author: author,
        content: content.value
      };
      if (window.sendUserAgent === true)
        doc.userAgent = window.navigator.userAgent;

      content.value = "";
      websocket.send(JSON.stringify(doc));
      // content.focus();
    }
    evt.stopPropagation();
    evt.preventDefault();
  });
  document.getElementById("content").focus();
}

function testWebSocket() {
  websocket = new WebSocket(wsUri + '?author=' + author);
  websocket.onopen = function(evt) { onOpen(evt) };
  websocket.onclose = function(evt) { onClose(evt) };
  websocket.onmessage = function(evt) { onMessage(evt) };
  websocket.onerror = function(evt) { onError(evt) };
}

function onOpen(evt) {
  writeToScreen("CONNECTED");
}
function onClose(evt) {
  writeToScreen("DISCONNECTED");
}
function onMessage(evt) {
  var event = JSON.parse(evt.data);
  writeToScreen('<span class="mongo-message">' + new Date().toLocaleString() + ': ' + evt.data +'</span>');
  writeMessage('<span>' + renderAuthor(event.author) + renderDateFromObjectId(event._id) + renderContent(event.content) + '</span>');
}
function onError(evt) {
  writeToScreen('<span class="error-message">ERROR:</span> ' + evt.data);
}

function writeToScreen(message) {
  var pre = document.createElement("p");
  pre.innerHTML = message;
  log.insertBefore(pre, log.firstChild);
}
function writeMessage(message) {
  var pre = document.createElement("p");
  pre.innerHTML = message;
  output.appendChild(pre);
  output.scrollTo(0, output.scrollHeight);
}
function renderDateFromObjectId(objectId) {
  return '[' + (new Date(parseInt(objectId['$oid'].substring(0, 8), 16) * 1000)).toLocaleString() + '] <br>';
}
function renderAuthor(author) {
  return author ? '<span class="author">' + author + '</span> ' : '';
}
function renderContent(content) {
  return '<span class="user-message">' + (content || '') +'</span>';
}

window.addEventListener("load", init, false);
