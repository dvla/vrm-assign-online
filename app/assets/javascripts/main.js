require.config({
    paths: {
        'jquery' : '../lib/jquery/jquery',
        'jquery-migrate': '../lib/jquery-migrate/jquery-migrate',
        'header-footer-only': '../lib/vehicles-presentation-common/javascripts/header-footer-only',
        'global-helpers': '../lib/vehicles-presentation-common/javascripts/global-helpers',
        'page-init': '../lib/vehicles-presentation-common/javascripts/page-init',
        'assign-page-init': 'assign-page-init'
    },
    // Make jquery-migrate depend on the loading of jquery
    "shim": {
        'jquery-migrate': ['jquery']
    }
});

require(["assign-page-init"], function(assignPageInit) {
    $(function() {
        assignPageInit.init();

        // If JS enabled hide summary details
        $('.details').hide();

        // Summary details toggle
        $('.summary').on('click', function() {
            $(this).siblings().toggle();
            $(this).toggleClass('active');
        });
    });
});
