// Define the dependency to page-init in common
define(['jquery', 'jquery-migrate', "page-init"], function($, jqueryMigrate, pageInit) {

    return {
        init: function() {
            // Call initAll on the pageInit object to run all the common js in vehicles-presentation-common
            pageInit.initAll();

            // If JS enabled hide summary details
            $('.details').hide();

            // Summary details toggle
            $('.summary').on('click', function() {
                $(this).siblings().toggle();
                $(this).toggleClass('active');
            });

            /* Testing stuff */
            $('.js-tooltip').hide();
            $('.js-has-tooltip').addClass('enabled');
            $(document).mouseup(function (e) {
                var tooltipContainer = $(".js-tooltip");
                if ( !tooltipContainer.is(e.target) && tooltipContainer.has(e.target).length === 0 && (!$('.js-has-tooltip').is(e.target)) )  {
                    tooltipContainer.hide();
                }
            });
            $('.js-has-tooltip').on('click', function() {
                var tooltip = $(this).attr('data-tooltip');
                $('.js-tooltip[data-tooltip="' + tooltip +'"]').addClass('js-tooltip-visible');
                $('.js-tooltip[data-tooltip="' + tooltip +'"]').toggle();

            });
            /* Testing stuff */
        }
    }
});
