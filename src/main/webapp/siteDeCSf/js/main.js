// Scroll totop button
var toTop = jQuery('#to-top');
toTop.click(function () {
	jQuery('html, body').animate({scrollTop: '0px'}, 800);
	return false;
});
// AOS
AOS.init();