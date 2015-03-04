require.config({
    paths: {
        'jquery': '../lib/jquery/jquery.min',
        'picturefill': '../lib/picturefill/picturefill.min'
    }
});

require(["jquery", "picturefill", "main"],function(jquery, main) {
    function autoTab(currentId, nextId) {
        var focusable = $('#' + currentId);
        focusable.keyup(function (e) {
            var maxlength = false;
            if ($(this).attr("maxlength")) {
                if ($(this).val().length >= $(this).attr("maxlength")) maxlength = true;
            }
            // Tab when the input is a key betweena a - z, A - Z and 0 - 9 and the maxlength is reached.
            // Do not auto tab when we come to the field by tabbing or shift-tabbing, otherwise it is impossible to
            // return to the field.
            var inp = String.fromCharCode(e.keyCode);
            if (/[a-zA-Z0-9-_ ]/.test(inp) && maxlength) {
                $('#' + nextId).focus();
            }
        });
    }

    autoTab('certificate-document-count', 'certificate-date');
    autoTab('certificate-date', 'certificate-time');
    autoTab('certificate-time', 'certificate-registration-mark');
});