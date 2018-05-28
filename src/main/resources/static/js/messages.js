'use strict';

var messageForm = document.querySelector('#messageForm');
var messageArea = document.querySelector('#messageArea');
var messageInput = document.querySelector('#message');
var deleteForms = document.getElementsByName("deleteForm");

var fileInput = document.getElementById("file_input");
var fileDelete = document.getElementById("file_delete");
var panelDeleteFile = document.getElementById("panel-delete-file");

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
        var message = createMessage(messageForm);
        messageArea.appendChild(message);
    } else if (messageForm.typeMessage.toString() === "DELETE") {
        var deleteElement = document.getElementById(messageForm.message.id);
        deleteElement.parentNode.removeChild(deleteElement);
    }
}

function createMessage(messageForm) {
    var messageElement = document.createElement('div');
    messageElement.setAttribute("id", messageForm.message.id);

    var header = createHeaderMessage(messageForm);
    messageElement.appendChild(header);

    var textMessage = createTextArea(messageForm.message.text);
    messageElement.appendChild(textMessage);

    if (messageForm.message.nameAttachedFile !== "") {
        var fileArea = createFileArea(messageForm);
        messageElement.appendChild(fileArea);
    }
    messageElement.appendChild(document.createElement("br"));
    return messageElement;
}

function createHeaderMessage(messageForm) {
    var messageHeader = createEmptyContainer("row");
    var userInfo = createUserNameField(
        messageForm.message.author.firstName,
        messageForm.message.author.lastName,
        "col-sm-6"
    );
    messageHeader.appendChild(userInfo);

    var dateInfo = createSimpleTextField(getDate(messageForm.message.date), "color: gray", "col-sm-5");
    messageHeader.appendChild(dateInfo);

    if (messageForm.message.author.id.toString() === idUser.toString()) {
        var deleteArea = createDeleteArea(messageForm);
        messageHeader.appendChild(deleteArea);
    }
    return messageHeader;
}

function createFileArea(messageForm) {
    var containerDownload = createEmptyContainer("row");
    var downloadButton = createButton("submit", "btn btn-link",
        "download", "File: " + messageForm.message.nameAttachedFile);
    var formDownload = createForm("/download_file", "post", "downloadForm",
        idTalk, messageForm.message.id, downloadButton);
    containerDownload.appendChild(formDownload);
    return containerDownload;
}

function createDeleteArea(messageForm) {
    var containerDelete = createEmptyContainer("col-sm-1");
    var buttonDelete = createButton("submit", "close", "delete", "&times;");
    var formDelete = createForm("/delete_message", "post", "deleteForm",
        idTalk, messageForm.message.id, buttonDelete);
    formDelete.addEventListener("submit", deleteMessage, true);
    containerDelete.appendChild(formDelete);
    return containerDelete;
}

function createTextArea(text) {
    var containerTextArea = createEmptyContainer("row");
    var textArea = createSimpleTextField(text, "color: black", "col-sm-12")
    containerTextArea.appendChild(textArea);
    return containerTextArea;
}

function createUserNameField(userName, userFamilyName, nameContainer) {
    var userNameField = document.createElement('dt');
    userNameField.setAttribute("style", "color: #007fff; font-size: 16px");
    userNameField.innerText = userName + " " + userFamilyName;
    var containerUserInfo = createEmptyContainer(nameContainer);
    containerUserInfo.appendChild(userNameField);
    return containerUserInfo;
}

function createSimpleTextField(text, style, nameContainer) {
    var textField = document.createElement('dd');
    textField.setAttribute("style", style);
    textField.innerText = text;
    var textContainer = createEmptyContainer(nameContainer);
    textContainer.appendChild(textField);
    return textContainer;
}

function getDate(dateStr) {
    var date = new Date(dateStr);
    var months = ["января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа",
        "сентября", "октября", "ноября", "декабря"];
    return date.getDate() + " " + months[date.getMonth()] + " " + date.getFullYear()
        + " " + date.getHours() + ":" + date.getMinutes();
}

function createEmptyContainer(className) {
    var container = document.createElement('div');
    container.setAttribute('class', className);
    return container;
}

function createInput(type, value, name) {
    var inputElement = document.createElement('input');
    inputElement.setAttribute('type', type);
    inputElement.setAttribute('value', value);
    inputElement.setAttribute('name', name);
    return inputElement;
}

function createButton(type, className, name, text) {
    var buttonElement = document.createElement('button');
    buttonElement.setAttribute('type', type);
    buttonElement.setAttribute('class', className);
    buttonElement.setAttribute('name', name);
    buttonElement.innerHTML = text;
    return buttonElement;
}

function createForm(action, method, name, talkInfo, messageInfo, button) {
    var formElement = document.createElement('form');
    formElement.setAttribute('action', action);
    formElement.setAttribute('method', method);
    formElement.setAttribute('name', name);
    var talkField = createInput("hidden", talkInfo, "idTalk");
    var messageField = createInput("hidden", messageInfo, "idMessage");
    formElement.appendChild(talkField);
    formElement.appendChild(messageField);
    formElement.appendChild(button);
    return formElement;
}

function changeFile(event) {
    if (fileInput.files[0].size > 131072) {
        event.target.value = "";
        var modalButton = document.getElementById("modalButton");
        modalButton.click();

    } else {
        file = fileInput.files[0];
        fileName = file.name;
        var viewName = document.getElementById("view-name-file");
        viewName.innerText = fileName;
        panelDeleteFile.setAttribute("class", "panel panel-default");
    }
}

function deleteFile(event) {
    fileName = "";
    file = null;
    fileInput.value = "";
    panelDeleteFile.setAttribute("class", "hidden");
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
        if (file) {
            sendFile(messageForm, file);
        }
        stompClient.send(sendAddress, {}, JSON.stringify(messageForm));
        messageInput.value = "";
        deleteFile();
    }
    event.preventDefault();
}

function sendFile(messageForm, file){
    var form = new FormData();
    form.append("idUser", idUser);
    form.append("idTalk", idTalk);
    form.append("date", messageForm.message.date.toString());
    form.append("file", file);

    var request = new XMLHttpRequest();
    request.onload = function () {
        console.log("Отправка завершена");
    };
    request.open("post", "/upload_file", true);
    request.send(form);
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
fileInput.addEventListener('change', changeFile, false);
fileDelete.addEventListener('click', deleteFile, false);
[].forEach.call(deleteForms, function (element) {
    element.addEventListener('submit', deleteMessage, true);
});
connect();
