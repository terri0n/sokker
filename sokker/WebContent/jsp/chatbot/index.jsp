<%@ page language="java" contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
  <head>
    <meta charset="UTF-8">
    <title>Sokker Chatbot</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/jsp/chatbot/style.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="${pageContext.request.contextPath}/jsp/chatbot/chatbot.js.jsp"></script>
  </head>
  <body>
    <div class="chat-container">
      <div class="row justify-content-md-center">
        <div class="col-md-6">
          <div class="card">
            <div class="card-header">
              Sokker Chatbot
            </div>
            <div class="card-body" id="container">
              <ul class="list-group" id="chat-messages">
              </ul>
            </div>
            <div class="card-footer">
              <form id="chat-form">
                <div class="form-group">
                  <input type="text" class="form-control" id="message-text" placeholder="Type your message...">
                </div>
                <button type="submit" class="btn btn-primary" id="btn-send">Send</button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </body>
</html>