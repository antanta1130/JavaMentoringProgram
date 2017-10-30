$(function() {

	function mailFiltrationHandler(event) {

		event.preventDefault();
				
		$.ajax({
			url : "/messages/filter?fromDate=" + $('#fromDate').val() + "&toDate=" + $('#toDate').val() + "&searchInput=" + $('#searchInput').val(),
			method : "GET",
			beforeSend : function(xhr) {
				xhr.setRequestHeader("Content-Type", "application/json");
			},
			dataType: "json"
		}).done(function(data) {
			console.log(data);
		}).fail(function(jqXHR, textStatus, errorThrown) {
			console.error(textStatus + ' Problems with search parameters...'
							+ errorThrown);
		});		

	}
	;
	        
	$('#filtrationButton').click(mailFiltrationHandler);
	
	jQuery(function(){
			 jQuery('#fromDate').datetimepicker({
			  format:'Y-m-d',
			  onShow:function( ct ){
			   this.setOptions({
			    maxDate:jQuery('#toDate').val()?jQuery('#toDate').val():false
			   })
			  },
			  timepicker:false
			 });
			 jQuery('#toDate').datetimepicker({
			  format:'Y-m-d',
			  onShow:function( ct ){
			   this.setOptions({
			    minDate:jQuery('#fromDate').val()?jQuery('#fromDate').val():false
			   })
			  },
			  timepicker:false
			 });
});

	
});