require(['config'], function() {
    require(["assign-page-init"], function(assignPageInit) {
        $(function() {
            assignPageInit.init();
        });
    });
});
