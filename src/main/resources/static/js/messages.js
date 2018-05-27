'use strict';

var messageForm = document.querySelector('#messageForm');
var messageArea = document.querySelector('#messageArea');
var messageInput = document.querySelector('#message');
var deleteForms = document.getElementsByName("deleteForm");

var fileInput = document.getElementById("file_input");
var idTalk = document.getElementById("id_talk").getAttribute("value");
var idUser = document.getElementById("id_user").getAttribute("value");
var subscribeAddress = "/topic/talk/" + idTalk;
var sendAddress = "/app/talk/" + idTalk + "/send_message";
var deleteAddress = "/app/talk/" + idTalk + "/delete_message";
var fileName = "";
var file = null;
var stompClient = null;

function connect() {
    var socket = new SockJS("/messenger");
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected);
}

function onConnected() {
    stompClient.subscribe(subscribeAddress, onMessageReceived);
}

function onMessageReceived(payload) {
    var messageForm = JSON.parse(payload.body);
    if (messageForm.typeMessage.toString() === "SEND") {
        var messageElement = document.createElement('fieldset');
        messageElement.setAttribute("id", messageForm.message.id);

        var dateElement = document.createElement('legend');
        var dateFormat = document.createTextNode(getDate(messageForm.message.date));
        dateElement.appendChild(dateFormat);

        var usernameElement = document.createElement('h3');
        var usernameText = document.createTextNode(messageForm.message.author.firstName + " " +
            messageForm.message.author.lastName);
        usernameElement.appendChild(usernameText);

        var textElement = document.createElement('p');
        var messageText = document.createTextNode(messageForm.message.text);
        textElement.appendChild(messageText);

        messageElement.appendChild(dateElement);
        messageElement.appendChild(usernameElement);
        messageElement.appendChild(textElement);

        if (messageForm.message.nameAttachedFile !== "") {
            var talkInfo1 = createInput('hidden', idTalk, 'idTalk');
            var messageInfo1 = createInput('hidden', messageForm.message.id, 'idMessage');
            var fileElement = document.createElement('p');
            var submitButton1 = createInput('submit', 'Download file', 'download');
            var nameFile = document.createTextNode(' ' + messageForm.message.nameAttachedFile);
            fileElement.appendChild(submitButton1);
            fileElement.appendChild(nameFile);
            var formDownload = createForm('http://localhost:8080/download_file', 'post');
            formDownload.appendChild(talkInfo1);
            formDownload.appendChild(messageInfo1);
            formDownload.appendChild(fileElement);
            messageElement.appendChild(formDownload);
        }
        if (messageForm.message.author.id.toString() === idUser.toString()) {
            var talkInfo2 = createInput('hidden', idTalk, 'idTalk');
            var messageInfo2 = createInput('hidden', messageForm.message.id, 'idMessage');
            var submitButton2 = createInput('submit', 'Delete message', 'delete');
            var formDelete = createForm('http://localhost:8080/delete_message', 'post');
            formDelete.addEventListener('submit', deleteMessage, true);
            formDelete.appendChild(talkInfo2);
            formDelete.appendChild(messageInfo2);
            formDelete.appendChild(submitButton2);
            messageElement.appendChild(formDelete);
        }
        messageArea.appendChild(messageElement);
        messageArea.appendChild(document.createElement('br'));
        messageArea.scrollTop = messageArea.scrollHeight;
    } else if (messageForm.typeMessage.toString() === "DELETE") {
        var deleteElement = document.getElementById(messageForm.message.id);
        deleteElement.parentNode.removeChild(deleteElement);
    }
}

function createInput(type, value, name) {
    var inputElement = document.createElement('input');
    inputElement.setAttribute('type', type);
    inputElement.setAttribute('value', value);
    inputElement.setAttribute('name', name);
    return inputElement;
}

function createForm(action, method) {
    var formElement = document.createElement('form');
    formElement.setAttribute('action', action);
    formElement.setAttribute('method', method);
    return formElement;
}

function onChangeFile(event) {
    if(fileInput.files[0].size > 131072) {
        event.target.value = '';
        alert("Limit on file size: 128 KB");
    } else {
        file = fileInput.files[0];
        fileName = file.name;
    }

    /*var reader = new FileReader();
    reader.onloadend = function () {
        fileContent = reader.result;
    };
    if (file) {
        reader.readAsDataURL(file);
    } else {
        fileContent = "";
    }*/
}

function getDate(dateStr) {
    var date = new Date(dateStr);
    var months = ["января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа",
        "сентября", "октября", "ноября", "декабря"];
    return date.getDate() + " " + months[date.getMonth()] + " " + date.getFullYear()
        + " " + date.getHours() + ":" + date.getMinutes();
}

function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {
        var messageForm = {
            typeMessage: "SEND",
            message: {
                text: messageContent,
                date: new Date(),
                author: {id: idUser},
                nameAttachedFile: fileName
            }
        };
        if(file){
            var form = new FormData();
            form.append("idUser", idUser);
            form.append("idTalk", idTalk);
            form.append("date", messageForm.message.date.toString());
            form.append("file", file);

            var xhr = new XMLHttpRequest();
            xhr.onload = function() {
                console.log("Отправка завершена");
            };
            xhr.open("post", "/upload_file", true);
            xhr.send(form);
        }

        stompClient.send(sendAddress, {}, JSON.stringify(messageForm));
        messageInput.value = "";
        fileInput.value = "";
        file = null;
        fileName = "";
    }
    event.preventDefault();
}

function deleteMessage(event) {
    var messageId = event.target.idMessage.value;
    if (messageId && stompClient) {
        var messageForm = {
            typeMessage: "DELETE",
            message: {id: messageId}
        };
        stompClient.send(deleteAddress, {}, JSON.stringify(messageForm));
    }
    event.preventDefault();
}

messageForm.addEventListener('submit', sendMessage, true);
fileInput.addEventListener('change', onChangeFile, false);
[].forEach.call(deleteForms, function (element) {
    element.addEventListener('submit', deleteMessage, true);
});
connect();
