$('#select1').on('change', function () {
    var selected = $(this).val()
    //if ($(this).val() == "English") {
    if ((selected == "All Languages") || (selected == "en") ||
        (selected == "es") || (selected == "pt") || (selected == "fr")) {
        $('#etapa1').hide();
        $('#etapa2').show();
        $('#etapa3').hide();
        $('#etapa4').hide();
        $('#etapa5').hide();
    }
});

$('#btEtapa2').on('click', function () {
    $('#etapa1').hide();
    $('#etapa2').hide();
    $('#etapa3').show();
    $('#etapa4').hide();
    $('#etapa5').hide();
});
$('#select2').on('change', function () {
    var selected = $(this).val()
        //if ($(this).val() == "English") {
    if ((selected == "All Languages") || (selected == "en") ||
        (selected == "es") || (selected == "pt") || (selected == "fr")) {
        $('#etapa1').hide();
        $('#etapa2').hide();
        $('#etapa3').hide();
        $('#etapa4').show(); 
        $('#etapa5').hide(); 
    }
});
$('#btEtapa3').on('click', function () {
    $('#etapa1').hide();
    $('#etapa2').hide();
    $('#etapa3').hide();
    $('#etapa4').show();
    $('#etapa5').hide();
});
$('#btEtapa4').on('click', function () {
    $('#etapa1').hide();
    $('#etapa2').hide();
    $('#etapa3').hide();
    $('#etapa4').hide();
    $('#etapa5').show();
});



$('.bc1').click(function(){
   $('#etapa1').show();
   $('#etapa2').hide();
   $('#etapa3').hide();
   $('#etapa4').hide();
   $('#etapa5').hide();
   $('#select1, #select2').val('Opciones');
})
$('.bc2').click(function(){
   $('#etapa1').hide();
   $('#etapa2').show();
   $('#etapa3').hide();
   $('#etapa4').hide();
   $('#etapa5').hide();
   $('#select1, #select2').val('Opciones');
})
$('.bc3').click(function(){
   $('#etapa1').hide();
   $('#etapa2').hide();
   $('#etapa3').show();
   $('#etapa4').hide();
   $('#etapa5').hide();
   $('#select1, #select2').val('Opciones');
})
$('.bc4').click(function(){
   $('#etapa1').hide();
   $('#etapa2').hide();
   $('#etapa3').hide();
   $('#etapa4').show();
   $('#etapa5').hide();
   $('#select1, #select2').val('Opciones');
})

$('#btEtapa4b').click()
