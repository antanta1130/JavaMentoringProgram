$(function() {

	function mailFiltrationHandler(event) {

		event.preventDefault();
		
				
		$.ajax({
			url : "/messages/filter?fromDate=" + encodeURIComponent($('#fromDate').val()) + "&toDate=" + encodeURIComponent($('#toDate').val()) + "&searchInput=" + encodeURIComponent($('#searchInput').val()),
			method : "GET",
			contentType: 'application/json',
			dataType: "json"
		}).done(function(data) {
			var table;	
			$.each(data, function(i, item) {
			table = table + "<tr> <th>" + item.title + "</th> <th>" + item.body + "</th> <th>" + item.receivedDate + "</th> <th>" + item.from + "</th> </tr>";
			});
			$('#results').append(table);
		}).fail(function(jqXHR, textStatus, errorThrown) {
			console.error(textStatus + ' Problems with search parameters...'
							+ errorThrown);
		});		

	}
	;
	        
	$('#filtrationButton').click(mailFiltrationHandler);
	
	jQuery(function(){
			 jQuery('#fromDate').datetimepicker({
			  format:'Y-m-d H:i:00',
			  onShow:function( ct ){
			   this.setOptions({
			    maxDate:jQuery('#toDate').val()?jQuery('#toDate').val():false
			   })
			  },
			  timepicker:true
			 });
			 jQuery('#toDate').datetimepicker({
			  format:'Y-m-d H:i:00',
			  onShow:function( ct ){
			   this.setOptions({
			    minDate:jQuery('#fromDate').val()?jQuery('#fromDate').val():false
			   })
			  },
			  timepicker:true
			 });
});

	
});