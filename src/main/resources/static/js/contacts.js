'use strict';

function onChangeContact(event) {
    var form = event.target;
    var formData = new FormData(form);

    var request = new XMLHttpRequest();
    request.open("post", form.action, true);

    request.onreadystatechange = function (e) {
        if(request.readyState === 4 && request.status === 200) {
            var button = form.button;
            if(button.value === "add") {
                button.setAttribute("value", "delete");
                button.innerText = "Delete from my contact";
                button.setAttribute("class", "btn btn-warning");
                form.setAttribute("action", "/delete_contact");
            } else {
                button.setAttribute("value", "add");
                button.innerText = "Add in my contact";
                button.setAttribute("class", "btn btn-success");
                form.setAttribute("action", "/add_contact");
            }
        }
    };
    request.send(formData);
    event.preventDefault();
}

var changeForms = document.getElementsByName("changeContact");
[].forEach.call(changeForms, function (element) {
    element.addEventListener('submit', onChangeContact, true);
});