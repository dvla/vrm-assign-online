require.config({
    paths: {
        'jquery': 'lib/jquery/jquery-1.9.1',
        'jquery-migrate': 'lib/jquery/jquery-migrate-1.2.1.min',
        'header-footer-only': 'header-footer-only',
        'form-checked-selection': 'form-checked-selection',
        'picturefill': 'picturefill.min'
    }
});

require(["jquery", "jquery-migrate", "header-footer-only", "form-checked-selection", "picturefill"],function($) {

    var IE10 = (navigator.userAgent.match(/(MSIE 10.0)/g) ? true : false);
    if (IE10) {
        $('html').addClass('ie10');
    }

    $(function() {

        // Disabled clicking on disabled buttons
        $('.button-not-implemented').click(function() {
            return false;
        });

        // jQuery fallback for HTML5 summary tag
        $('summary ~ div').hide();
        $('summary').click(function() {
            $('summary ~ div').toggle();
        });

        // Print button
        $('.print-button').click(function() {
            window.print();
            return false;
        });

        // smooth scroll
        $('a[href^="#"]').bind('click.smoothscroll', function (e) {
            e.preventDefault();
            var target = this.hash,
                $target = $(target);
            $('html, body').animate({
                scrollTop: $(target).offset().top - 40
            }, 750, 'swing', function () {
                window.location.hash = target;
            });
        });


        // Picture element HTML shim|v it for old IE (pairs with Picturefill.js)
        // Example of using this script:
        // https://googlesamples.github.io/web-fundamentals/samples/media/images/media.html
        // https://scottjehl.github.io/picturefill/
        // https://scottjehl.github.io/picturefill/examples/demo-01.html
        document.createElement("picture");
    });

    function areCookiesEnabled(){
        var cookieEnabled = (navigator.cookieEnabled) ? true : false;

        if (typeof navigator.cookieEnabled == "undefined" && !cookieEnabled)
        {
            document.cookie="testcookie";
            cookieEnabled = (document.cookie.indexOf("testcookie") != -1) ? true : false;
        }
        return (cookieEnabled);
    }

    function opt(v){
        if (typeof v == 'undefined') return [];
        else return[v];
    }
});
