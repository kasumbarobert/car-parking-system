var k=document.getElementById;

$("#otherSignup").click(function(){
	var name=$("#sgnName").val();   
	var email=$("#sgnEmail").val();
	var password1=$("#sgnPassword1").val();
	var password2=$("#sgnPassword2").val();
	if(password1!=password2){
		alert("please Make passwords Must match");
		return false;
	}
	else if(name=="" || email=="" || password1=="" ||  password2==""){
		alert("some fields were not filled");
	}
	//after validation then use ajax to insert values into the db
	else{
		var dataString="name="+name+"&email="+email+"&password="+password1;
		$.ajax({
		type:"POST",
		url:"registeruser.php",
		data:dataString,
		success:function(){
			alert("you have succefully registered");
			//then empty the fields
			
			
			document.getElementById("sgnName").value=""
			document.getElementById("sgnEmail").value=""
			document.getElementById("sgnPassword1").value=""
			document.getElementById("sgnPassword2").value=""
		}
		
		
		});
		
		
	}
});
/*the above section deals with sign up for other site users ie. users that are new to the system*/


//comment  guestName  guestEmail choosenArticle  postComment


$("#postComment").click(function(){
	
	var comment=$("#comment").val();
	var logedUserzMail=$("#logedUserzMail").val();
	
	if(logedUserzMail=="" ){
		
		alert("please you signup or login to contribute");
		
	}
	else if(comment==""){
		alert("please type your comment");
	}
	else{
		var dataString="comment="+comment+"&logedUserzMail="+logedUserzMail;
		$.ajax({
			type:"POST",
			url:"insertComment.php",
			data:dataString,
			success:function(){
				//alert("your comment has been posted");
				document.getElementById("comment").value=""
			}
			
			
		});
		}
	});
	
	
	



