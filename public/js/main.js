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
    var title = document.getElementById("title");
    var content = document.getElementById("content");
    var doc = { author: author };
    if (title && title.value && title.value.length > 0) doc.title = title.value;
    if (content && content.value && content.value.length > 0) {
      doc.content = content.value;
      content.value = "";
    }
    websocket.send(JSON.stringify(doc));
    evt.stopPropagation();
    evt.preventDefault();
    content.focus();
  });
  document.getElementById("content").focus();
}

function testWebSocket() {
  websocket = new WebSocket(wsUri);
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
  writeMessage('<span><span class="author">' + event.author + '</span> [' + dateFromObjectId(event._id).toLocaleString() + '] <br><span class="user-message">' + (event.content || '') +'</span></span>');
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
function dateFromObjectId(objectId) {
  return new Date(parseInt(objectId['$oid'].substring(0, 8), 16) * 1000);
}

window.addEventListener("load", init, false);
