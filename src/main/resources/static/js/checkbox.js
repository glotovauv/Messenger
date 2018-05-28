function changeChoose(event) {
    var box = event.target;
    var check = document.getElementById(box.id + "-check");
    if (check){
        check.checked = !check.checked;
        var badge = document.getElementById(box.id + "-badge");
        if(check.checked) {
            box.setAttribute("class", "list-group-item list-group-item-info");
            badge.setAttribute("class", "badge");
        }
        else {
            box.setAttribute("class", "list-group-item");
            badge.setAttribute("class", "badge hidden")
        }
    }
}