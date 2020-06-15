$(document).ready(function () {
	let notebookMap = new Map();
	let notebooks = [];
	$("#notebookDiv").hide();
	$("#connectBtn span").text("Connect");
	$("#notebooks").change(function () {
        let selectedNotebook = $(this).val();
        console.log("selected notebook: "+selectedNotebook);
        $.fn.loadNotebook(selectedNotebook);
    });
	$("#notesTable").on('click','tr',function(e) { 
		let rowId = $(this).attr('id');
		var isInfoField = rowId.startsWith("info");
		if(isInfoField == false){
			let infoId = "info"+$(this).attr('id');
		    var result_style = document.getElementById(infoId).style;
		    if(result_style.display == 'table-row'){
		    	result_style.display = 'none';
		    }else{
		    	result_style.display = 'table-row';
		    	document.getElementById(infoId).style.backgroundColor = "coral";
		    }
		}
	}); 
	$.fn.loadNotebook = function(selectedNotebook) {
		$("#notesTbody").empty();
		let noteArr = notebookMap.get(selectedNotebook);
		for(i in noteArr){
			var key = noteArr[i].key;
			var value = noteArr[i].value;
			var info = noteArr[i].info;
			if(key == null){
				key = "";
			}
			if(value == null){
				value="";
			}
			if(info == null){
				info="";
			}
			$("#notesTbody").append('<tr id="'+noteArr[i].id+'"><td>'+key+'</td><td>'+value+'</td></tr>');
			$("#notesTbody").append('<tr id="info'+noteArr[i].id+'" style="display: none;"><td colspan=2>'+info+'</td></tr>');
		}
	  }
	
    $("#login-form").submit(function (event) {

        // stop submit the form event. Do this manually using ajax post function
        event.preventDefault();
        
        var btnText = $("#connectBtn").text();
        console.log(btnText);
        if(btnText == 'Disconnect') {
        	$("#notebookDiv").hide();
        	$("#notesTableDiv").hide();
        	$("#connectBtn span").text("Connect");
        	return;
        } 

        let userId = $("#userId").val();
        let userSecret = $("#userSecret").val();

        $("#connectBtn").prop("disabled", true);
        
        $.ajax({
            type: "GET",
            contentType: "application/json",
            url: "/getUserNotes",
            dataType: 'json',
            headers: {
                "Authorization": "JNOTES " + btoa(userId + ":" + userSecret)
              },
            cache: false,
            timeout: 600000,
            success: function (data) {
            	var notes =data.noteEntries;
            	let notebookSet = new Set();
            	for (i=0, len = notes.length; i<len; i++) {
            		var noteEntry = notes[i];
            		let noteArr = [];
            		if(notebookMap.has(noteEntry.notebook) ==true){
            			noteArr = notebookMap.get(noteEntry.notebook);
            		}
            		noteArr.push(noteEntry);
            		
            		notebookMap.set(noteEntry.notebook, noteArr);
            		
            		console.log(notebookMap);
            		// console.log(noteEntry.notebook);
            		notebookSet.add(noteEntry.notebook);
            	}
            	notebooks = Array.from(notebookSet);
            	console.log(notebooks);
            	// $( "#notebooksDropdown" ).load(window.location.href + "
				// #notebooksDropdown" );
            	$('#feedback').html("");
            	$("#notebookDiv").show();
            	for(i in notebooks){
            		var options="<option value="+notebooks[i]+">"+notebooks[i]+"</option>";
                    $(options).appendTo('#notebooks'); 
            	}
            	$("#notesTableDiv").show();
            	let selectedNotebook = notebooks[0];
            	$.fn.loadNotebook(selectedNotebook);

                console.log("SUCCESS : ", data);
                $("#connectBtn").prop("disabled", false);
                $("#connectBtn span").text("Disconnect");

            },
            error: function (e) {
            	console.log(e);
            	var errorMessage = "";
            	if(e.status == 404) {
            		errorMessage = "User not found";
            	} else if(e.status == 401) {
            		errorMessage = "Incorrect credentials";
            	} else {
            		errorMessage = "Unauthorized";
            	}
                var json = "<h4>Error</h4><pre>"
                    + errorMessage + "</pre>";
                $('#feedback').html(json);

                console.log("ERROR : ", e);
                $("#connectBtn").prop("disabled", false);

            }
        });
        
    });

});
