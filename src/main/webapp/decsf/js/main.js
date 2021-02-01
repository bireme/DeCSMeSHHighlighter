//preprint
$('.btDisclaimer').click(function(){
  $(this).parent().find(".fa-angle-down").toggleClass("fa-angle-up");
  $('#disclaimer').toggleClass("disclaimer");
})