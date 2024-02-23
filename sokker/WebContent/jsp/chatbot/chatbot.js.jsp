$(function () {
  $('#message-text').focus();

  // Function to generate HTML for user message
  function createUserMessageHTML(text) {
    return `
      <div class="row message-body">
        <div class="col-sm-12 message-main-sender">
          <div class="sender">
            <span class="message-time pull-right">
              \${new Date().toLocaleTimeString('es-ES', { hour: 'numeric', minute: 'numeric', hour12: false })}
            </span>
            <div class="message-text">\${text}</div>
          </div>
        </div>
      </div>
    `;
  }

  // Function to generate HTML for bot message
  function createBotMessageHTML(text) {
    return `
      <div class="row message-body">
        <div class="col-sm-12 message-main-receiver">
          <div class="receiver">
            <span class="message-time pull-right">
              \${new Date().toLocaleTimeString('es-ES', { hour: 'numeric', minute: 'numeric', hour12: false })}
            </span>
            <div class="message-text">\${text}</div>
          </div>
        </div>
      </div>
    `;
  }

  // Function to generate HTML for error message
  function createErrorMessageHTML(text) {
    return `
      <div class="row message-body">
        <div class="col-sm-12">
          <div class="error">
            <span class="message-time pull-right">
              \${new Date().toLocaleTimeString('es-ES', { hour: 'numeric', minute: 'numeric', hour12: false })}
            </span>
            <div class="message-text">\${text}</div>
          </div>
        </div>
      </div>
    `;
  }

  // Function to add a message to the chat window
  function addMessageToChat(messageHTML) {
    $('#chat-messages').append(messageHTML);
    $('#container').scrollTop($('#container')[0].scrollHeight);
    $('#message-text').focus();
  }

  // Event listener for chat form submission
  $('#chat-form').submit(function(event) {
    event.preventDefault();
    const messageText = $('#message-text').val().trim();
    if (messageText === '') {
      return;
    }
    addMessageToChat(createUserMessageHTML(messageText));
    $('#message-text').val('');

    // Send message to server for processing
    $.post('${pageContext.request.contextPath}/chatbot', { message: messageText }, function(response) {
      addMessageToChat(createBotMessageHTML(response));
    }).fail(function(jqXHR) {
  		// Error handle
	    addMessageToChat(createErrorMessageHTML(jqXHR.responseText));
	});

  });

});
