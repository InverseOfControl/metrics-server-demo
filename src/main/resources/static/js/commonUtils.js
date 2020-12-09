const CommonUtils = {
    // 弹框
    alert: function (msg, type) {
        const divElement = $("<div></div>");
        divElement.addClass("alert alert-success col-md-2");
        divElement.css({
            "position": "absolute",
            "top": "40px",
            "z-index": "1051",
            "left": "50%",
            "margin-left": "-140px"
        })
        divElement.text(msg);

        $("body").append(divElement);

        setTimeout(function () {
            divElement.remove();
        }, 1500);
    }
}