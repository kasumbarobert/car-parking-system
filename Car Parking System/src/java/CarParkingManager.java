

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class CarParkingManager extends HttpServlet {
    private  Connection con;
    private Statement stmt;
    
    public void init(ServletConfig conf) throws ServletException{
        
        /*Initialising the database connection whenever the Servlet is created*/
         try{
            Class.forName("com.mysql.jdbc.Driver");
            con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost/xyz_db", "root", "");
                stmt = (Statement) con.createStatement();
            }
            catch(ClassNotFoundException e  )
            {
                System.exit(0);
            }
            catch(SQLException e)
            {
            }
    }

    
    @Override
    public void destroy()
    {
        try{
            
            con.close();
        
        }
        catch(SQLException e)
        {
            
        }
    }
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            
            /*
            Everytime the servlet loads, the two methods computeFreeHours and updateDatabaseInfo are called
            * compute free hours checks for all the cars in the databases and calculates whether they have spent over 30%(50.4)
                of the total hours(168) in the last seven consecutive days.
              if a car qualifies for free hours, it is given 10% of the total time it spent in parking, this value is stored
              in the database and it affects the amount to be charged on the car for the following day(s)
            
            * updateDatabaseInfo logs all the cars in parking by calculating the time it spend the previous day(up to the end of yesterday)
              The date, total hours spent(without considering free hours), chargeable hours(considering free hours), the total amount to charge for the 
              previous day are all stored in the database.
              It does not calculate for the current day because we are not sure yet how much time the car will spend today, we can only be
              sure if the day ends and we start another day before it has left.
              When a car is removed/dispatched/released from parking then the function releaseCar calculates and logs the
              information for the current day
               
            */
            computeFreeHours(out);
            updateDatabaseInfo(out);
            
            //start of the html document
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<link href=\"xyzstyles/css/bootstrap.min.css\" rel=\"stylesheet\" />\n" +
                        "<link href=\"xyzstyles/css/ourStyles.css\" rel=\"stylesheet\" />\n" +
                        "<script src=\"xyzstyles/js/bootstrap.js\"></script>\n" +
                        "<script src=\"xyzstyles/js/bootstrap.min.js\"></script>\n" +
                        "<script src=\"xyzstyles/js/npm.js\"></script>\n" +
                        "<script src=\"xyzstyles/js/dropdown.js\"></script>\n" +
                        "<script src=\"xyzstyles/js/vendor/jquery.min.js\"></script>\n" +
                        "<script src=\"xyzstyles/js/transition.js\"></script>\n" +
                        "<script src=\"xyzstyles/js/dropdown.js\"></script>\n" +
                        "<script src=\"xyzstyles/js/modal.js\"></script>\n" +
                        "<script src=\"xyzstyles/js/tooltip.js\"></script>\n" +
                        "<script src=\"xyzstyles/js/popover.js\"></script>\n" +
                        "<script src=\"xyzstyles/js/submitFormsData.js\"></script>\n" +
                        "<script src=\"xyzstyles/js/jquery1.js\"></script>\n" +
                        "\n" +
                        "<link rel=\"stylesheet\" href=\"xyzstyles/f-awm/css/font-awesome.css\" />");
            out.println("<style>#header {\n" +
                        "background-image: url(images/pic5.png); background-size: cover;padding-top:2%; padding-bottom:2s%; color:#fff;\n" +
                        "}</style>");
            out.println("<title>XY Car Parking System</title>");            
            out.println("</head>");
            out.println("<body  style='background:grey; background-image:url(images/bg.jpg);' >");
            
            out.println("<div style='background:white;' class='container'>");
            
            out.println("<div class='row'>");
            out.println("<div class='col-lg-12 ' style='padding:0px;'>");
            /*Banner*/
            out.println("<img src='images/pic5.png' width='100%' class='img-responsive'/>");
            out.println("</div>");
            
            
            out.println("</div>");
            /*The side bar the contains the menu list*/
            out.println("<div class='row'>");
            out.println("<div class='col-lg-3 list-group panel panel-default' style=' padding:2%; background-color:#abd; '>");
            out.println("<a href='/xyz/CarParkingManager' class='list-group-item'>");
            out.println("<h2 align='center'><i class='fa fa-home'></i> Main Menu</h2>");
            out.println("</a>");
            out.println("<a href='/xyz/CarParkingManager?action=register_car' class='list-group-item'><div>");
            out.println("<h4>Register New Car</h4>");
            out.println("</div></a>");
            out.println("<a href='/xyz/CarParkingManager?action=view_spaces' class='list-group-item'><div>");
            out.println("<h4>Parking Yards</h4>");
            out.println("</div></a>");
            out.println("<a href='/xyz/CarParkingManager?action=view_cars_available' class='list-group-item'><div>");
            out.println("<h4>View Cars in Parking</h4>");
            out.println("</div></a>");
            out.println("<a href='/xyz/CarParkingManager?action=add_new_parking' class='list-group-item'><div>");
            out.println("<h4>New Parking Yard</h4>");
            out.println("</div></a>");
            out.println("<a href='/xyz/CarParkingManager?action=release_car' class='list-group-item'><div>");
            out.println("<h4>Release Car</h4>");
            out.println("</div></a>");
            out.println("<a href='/xyz/CarParkingManager?action=car_logs' class='list-group-item'><div>");
            out.println("<h4>View Car Logs</h4>");
            out.println("</div></a>");
            out.println("<a href='/xyz/CarParkingManager?action=cell_totals' class='list-group-item'><div>");
            out.println("<h4>Cell Performance</h4>");
            out.println("</div></a>");
            out.println("</div>");
            out.println("<div class='col-lg-9 panel panel-default' style='margin:0;padding:3%;'>");
            
            /*each of the menu list items has a different action value and basing on the action value
              a different function/method is called. in the service method
              By default, the method homepage content is called
            */
            String action = request.getParameter("action");
            
            if(action != null)
            {
                if(action.equals("view_spaces"))
                {
                    displayParkingYards(out,request);
                }
                else if(action.equals("release_car"))
                {
                    releaseCar(out,request);
                    displayCars(out);
                }
                else if(action.equals("register_car"))
                {
                    /*this function provides the interface to register a new car and assign it to a parking cell in a chosen parking yard*/
                    handleCarAssignment(out,request);
                }
                else if(action.equals("view_cars_available"))
                {
                    
                    //refreshes the page after 2 minutes to display the updated hours spent and amount payable
                   response.setIntHeader("refresh", 120);
                    displayCars(out);
                }
                else if(action.equals("car_logs"))
                {
                    /*it provides an interface that the user uses to select a given and then it displays the logs
                      for that car and how much it was charged for the different days before today
                    */
                    viewCarLogs(out,request);
                }
                else if(action.equals("add_new_parking"))
                {
                    //displays the interface to handle process of registering and editing parking yards
                    addNewParkingYard(out,request);
                }
                else if(action.equals("cell_totals"))
                {
                    //displays with the highest money amounts accumulated over a chosen period of time
                    
                    displayCells(out,request);
                }
                else{
                    homePageContent(out,request, response);
                }

            }
            else{
                   homePageContent(out,request, response);
            }
            
            
            //
           
            out.println("</div>");
            out.println("</div>");
            
            out.println("<div class='row'>");
            out.println("<div class='col-lg-12'>");
            out.println("<center>&copy;2016 Copyrights reserved for XY Associates</center>");
            out.println("</div>");
            out.println("</div>");
            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
        }
        
        
    }
    /**
     * 
     * @param t
     * @param request
     * @param response 
     * 
     * 
     */
    private void homePageContent(PrintWriter t, HttpServletRequest request, HttpServletResponse response)
    {
        /*
        
        *Using cookies to check whether the user is visiting for the first time or for another time
        *If the user is visiting for the first time ever, a welcome message is displayed else it is no
        */
        Cookie [] cookies = request.getCookies();
        boolean cookie_found=false;
        
        for(Cookie x : cookies)
        {
            if(x.getName().equals("return_user"))
            {
                cookie_found = true;
                break;
            }
        }
        
        if(!cookie_found)
        {
            t.println("<h2 class='text text-success'>Hi! Welcome to the XY & associates car parking Manager</h2>");
            
            Cookie return_user = new Cookie("return_user", "xyyusjsgsgsv");
            
            return_user.setMaxAge(30*24*3600);
            
            response.addCookie(return_user);
        }
        else{
            Cookie return_user = new Cookie("return_user", "xyyusjsgsgsv");
            
            return_user.setMaxAge(30*24*3600);
            
            response.addCookie(return_user);
        }
        //refreshes the page after 2 minutes to display the updated hours spent and amount payable
        response.setIntHeader("refresh", 120);
        displayCars(t);
    }
    /**
     * 
     * @param t
     * @param request 
     * 
     * addNewParkingYard provides the forms to create and edit a parking yard
     * It also has the logic to create new parking yards with their parking cells and updating the database
     */
    private void addNewParkingYard(PrintWriter t, HttpServletRequest request)
    {
        String yard=request.getParameter("parking_yard");
        if(yard==null){
        String script;
            script = "<script type='text/javascript'>\n" +
                    "            function evenNumbers(id)\n" +
                    "            {\n" +
                    "               var number=document.getElementById(id).value;\n" +
                    "                if((number%2)==0){}\n"+
                    "                else{alert('enter only even values!');"
                    + "document.getElementById(id).value='';\n}\n" +
                    "            }\n"+
                    "        </script>";
       t.println(script);
       
       String yard_to_edit = request.getParameter("yard_to_edit");
       /*
       Form to display information of the selected yard
       */
       if(yard_to_edit != null)
        {
            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM parking_yard WHERE Name ='"+yard_to_edit+"'");
                rs.next();
                String edit_form ="<div style='font-size:1.5em; padding:2%;  background-color:#adb; margin:3%;' class='row' >"
               + "<div class='col-lg-12'><form method='post' action=''>"
               + "<h3>Editing "+rs.getString(1)+" yard</h3>"
               + "<p><label>Parking Yard Name </label><input type='text' name='yard_name' value='"+rs.getString(1)+"'  disabled/></p>'"
                        + "<input type='hidden' name='new_yard_name' value='"+rs.getString(1)+"'/>"
               + "<p><label>Parking Yard Location </label><input type='text' name='new_yard_location' value='"+rs.getString(2)+"' required='required'/></p>"
               + "<p><label>Add new slots</label><input type='number' name='add_num' value='0' min=0 id='add_num' onChange='evenNumbers(\"add_num\");' required='required'/><span style='font-size:0.6em;' > * enter an even number of slots</span></p>"
               + "<p><label>Current Number of cells(slots): "+rs.getString(3)+" <input type='hidden' name='current_cells' value='"+rs.getString(3)+"'/></p>"
                + "<input type='submit' value='save' class='btn btn-default'/>"
                + "</form></div></div>";
                t.println(edit_form);
               
            } catch (SQLException ex) {
                t.println(ex.getMessage());
            }
        }
       /*
       Form to be used to add a new parking yard specifying the yard name, location and number of parking cell 
       */
       String new_yard_name = request.getParameter("new_yard_name");
       if(new_yard_name != null)
       {
           try{
           String new_location = request.getParameter("new_location");
           int add_cells = Integer.parseInt(request.getParameter("add_num"));
           int total_cells =Integer.parseInt(request.getParameter("current_cells"))+add_cells; 
           if(add_cells>0)
           {
               ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) FROM parking_space");
               rs2.next();
                int available_cells_num = rs2.getInt(1);
                int k =1;
                int counter = available_cells_num;
                String cell;
                while(k<=add_cells)
                {
                    cell="cell_"+counter;
                    counter++;
                    stmt.execute("INSERT INTO parking_space VALUES('"+cell+"','0','','"+new_yard_name+"')");
                    k++;
                }
                stmt.executeUpdate("UPDATE parking_yard SET Location='"+new_location+"', Number_Of_Cells = '"+total_cells+"' WHERE Name='"+new_yard_name+"' ");
                
                t.print("<h1 class='text text-success'> Yard "+new_yard_name+" has been updated successfully</h1>");
           }
           }
           catch(SQLException ex)
           {
               t.println(ex.getMessage());
           }
           
       }
       String parking="<div style='font-size:1.5em; padding:2%;  background-color:#abd;' class='row' >"
               + "<div class='col-lg-12'><form action='' for='form' method='post'>"
               + "<h3>Fill the form below to create a new parking yard</h3>"
                      +"<p><label>Parking Yard Name:</label> <input type='text' name='yard_name' required/></p>"
                      +"<p><label>Parking Yard Location:</label> <input type='text' name='yard_location' required/></p>"
                      +"<p><label>Number of slots:</label> <input type='number' name='slotnumber' id='slot_number' min=2 onChange='evenNumbers(\"slot_number\");'  required/> <span style='font-size:0.6em;'> * enter an even number of slots</span></p>"
                      +"<input type='hidden' name='parking_yard' value='parking_yard'/>"
                      +"&nbsp; &nbsp; &nbsp; <input type='submit' value='submit'/>"
                      + "&nbsp; &nbsp; &nbsp; <input type='reset' value='Clear'/>"
                      +"</form></div></div>";
      
       
       t.println(parking);
       /*form to provide to enable the user choose the yard to edit, after it has been submitted the user will be provided with 
         another form to help him/her edit the yard
       */
       String edit_parking_yard ="<div style='font-size:1.5em; padding:2%;  background-color:#abd; margin:3%;' class='row' >"
               + "<div class='col-lg-12'>"
               + "<h3>Yard Edit Form</h3>"
               + "<form action='' for='form' method='post'>"
               + "Choose the yard to edit <select name='yard_to_edit'>";
       ResultSet yards=null;
            try {
                yards = stmt.executeQuery("SELECT Name From parking_yard");
                while(yards.next())
                {
                    edit_parking_yard+="<option value='"+yards.getString("Name")+"'>"+yards.getString("Name")+"</option>";
                }
            } catch (SQLException ex) {
                t.println(ex.getMessage());
            }
            edit_parking_yard+="</select><input type='submit' value='Edit'/>"
                    + "</form></div></div>";
       t.println(edit_parking_yard);
       
        }
        else{
            /*picking data submitted in the form so as to create the parking yard with its corresponding parking cells*/
            int num_of_cells = Integer.parseInt(request.getParameter("slotnumber"));
            String yard_name = request.getParameter("yard_name");
            String yard_location = request.getParameter("yard_location");
            
            try {
                ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM parking_space");
                rs1.next();
                int available_cells = rs1.getInt(1);
                
                String []slots = new String[num_of_cells];
                int counter = available_cells;
                int addSpace = stmt.executeUpdate("INSERT INTO parking_yard VALUES('"+yard_name+"','"+yard_location+"','"+num_of_cells+"')");
                
                //stmt.executeU
                if(addSpace>0)
                {
                for(String cell : slots)
                {
                    cell="cell_"+counter;
                    counter++;
                    stmt.execute("INSERT INTO parking_space VALUES('"+cell+"','0','','"+yard_name+"')");
                }
                t.print("<h1 class='text text-success'> The Yard "+yard_name+" has been created successfully with "+num_of_cells+" cells</h1>");
                
                }
                
            } catch (SQLException ex) {
               t.println(ex.getMessage());
            }
            
            
        }
    
}
    
    /*
    
        *The method releaseCar supports the process of removing a car from parking
        *It displays a form to enable the user choose the car to dispatch/remove
        *It also displays the list of all car available in parking by calling the method displayCars
        *it calculates the hours spent on the last day and logs into "daily_logs" table as well as the amount payable
        *It computes the total numbers of hours spent as well as the total amount to pay
        *It sets free the parking cells to be used by other cars
        *it also displays a car dispatch slip with the information about the car
    
    */
    
    private void releaseCar(PrintWriter t, HttpServletRequest request) 
    {
        String dispatched_car = request.getParameter("dispatch_car");
        
        if(dispatched_car == null){
        t.println("<div class='row' style='font-size:1.5em; background-color:#ccc; padding:2%;'><div class='col-lg-12'>");
        t.println("<form action='' method='post' for='form' style='font-size:1.4em;'>");
        t.println("<label>Choose the car to dispatch:</label> <select name='dispatch_car' required='required'> ");
       
        try {
            ResultSet car_in_parking = stmt.executeQuery("SELECT DISTINCT Registration_Number  FROM car_logs WHERE Departure_Date='1930-01-01'");
            while(car_in_parking.next())
            {
                t.println("<option value='"+car_in_parking.getString(1)+"'>"+car_in_parking.getString(1)+"</option>");
            }
        } catch (SQLException ex) {
            t.println(ex.getMessage());
        }
        t.println("</select>");
        t.println("<input type='submit' name='submit' value='Dispatch Car' class='btn btn-primary'/>");
        t.println("</form>");
         t.println("</div></div>");
        }
        else{
            
            try {
            ResultSet get = stmt.executeQuery("SELECT Arrival_Date, Arrival_Time FROM car_logs WHERE Registration_Number='"+dispatched_car+"' AND Departure_Date='1930-01-01'");
            get.next();
            
            Calendar now = new GregorianCalendar();
            Calendar start_of_today = new GregorianCalendar();
            String month = String.valueOf(start_of_today.get(Calendar.MONTH)+1);
            if(month.length()<2)
            {
                month="0"+month;
            }
            String x_date =start_of_today.get(Calendar.YEAR)+"-"+
            month+"-"+
            start_of_today.get(Calendar.DAY_OF_MONTH);
            //t.println(x_date + " "+ get.getString("Arrival_Date"));
            String arrival_date1 = get.getString("Arrival_Date");
            String arrival_time1 = get.getString("Arrival_Time");
            if(x_date.equals(get.getString("Arrival_Date"))){
                String [] time = get.getString("Arrival_Time").split(":");
                start_of_today.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
                start_of_today.set(Calendar.MINUTE, Integer.parseInt(time[1]));
                start_of_today.set(Calendar.SECOND, Integer.parseInt(time[2]));
            }
            else{
    
                start_of_today.set(Calendar.HOUR_OF_DAY, 0);
                start_of_today.set(Calendar.MINUTE, 0);
                start_of_today.set(Calendar.SECOND, 0);
            }
            
            float hours_covered_today = (float)((now.getTimeInMillis()-start_of_today.getTimeInMillis())/3600000.00);
                    
           
            String x_time =now.get(Calendar.HOUR_OF_DAY)+":"+
            now.get(Calendar.MINUTE)+":"+
            now.get(Calendar.SECOND);
            float hours = hours_covered_today;
            ResultSet rs1= stmt.executeQuery("SELECT Bonus_Hours FROM car_details WHERE Registration_Number='"+dispatched_car+"'");
            rs1.next();
            float free_hours = rs1.getFloat("Bonus_Hours");
            
            if(hours_covered_today>=free_hours && free_hours !=0)
            {
                hours_covered_today-=free_hours;
                free_hours=0;
                stmt.execute("UPDATE car_details SET Bonus_Hours='"+free_hours+"'  WHERE Registration_Number= '"+dispatched_car+"'");
            }
            else if(free_hours>0)
            {
                free_hours-=hours_covered_today;
                hours_covered_today=0;
                stmt.execute("UPDATE car_details SET Bonus_Hours='"+free_hours+"'  WHERE Registration_Number= '"+dispatched_car+"'");
            }
            ResultSet amount;
            
                amount = stmt.executeQuery("SELECT Hourly_Charge_Fee,Space_Occupied, Arrival_Date FROM car_logs WHERE Registration_Number='"+dispatched_car+"' AND Departure_Date='1930-01-01'");
            
            amount.next();
            
            String cell = amount.getString("Space_Occupied");
            String arrival_date = amount.getString("Arrival_Date");
            float charge = hours_covered_today>9?(float)(amount.getFloat(1)*0.89*hours_covered_today):(float)(amount.getFloat(1)*hours_covered_today);
           // t.println("INSERT INTO daily_logs(Registration_Number,Log_Day, Charge_Fee,Space_Occupied,Hours_Spent_Today,Total_Hours_Spent_Today) VALUES('"+dispatched_car+"','"+x_date+"','"+charge+"','"+cell+"','"+hours_covered_today+"','"+hours+"'");
            stmt.executeUpdate("INSERT INTO daily_logs(Registration_Number,Log_Day, Charge_Fee,Space_Occupied,Hours_Spent_Today,Total_Hours_Spent_Today) VALUES('"+dispatched_car+"','"+x_date+"','"+charge+"','"+cell+"','"+hours_covered_today+"','"+hours+"')");
           // t.println("After here");
            stmt.executeUpdate("UPDATE parking_space SET Space_Status='0', Car_in_Space=' ' WHERE Space_ID='"+cell+"'");
            //stmt.executeUpdate("UPDATE car_logs SET Departure_Date='"+x_date+"' WHERE Registration_Number='"+dispatched_car+"' AND Departure_Date='1930-01-01'");
            ResultSet rs = stmt.executeQuery("SELECT Charge_Fee, Total_Hours_Spent_Today FROM daily_logs WHERE Registration_Number='"+dispatched_car+"' AND Log_Day>='"+arrival_date+"'");
            //t.println("arrival date is "+arrival_date);
            float total_charge =0;
            float total_hours =0;
            while(rs.next())
            {
                total_charge+=rs.getFloat("Charge_Fee");
                total_hours+=rs.getFloat("Total_Hours_Spent_Today");
            }
            stmt.executeUpdate("UPDATE car_logs SET Departure_Date='"+x_date+"',Departure_Time='"+x_time+"',Amount='"+total_charge+"', Total_Hours_Spent='"+total_hours+"',logged='1' WHERE Registration_Number='"+dispatched_car+"' AND Departure_Date='1930-01-01'");
            
            t.println("<div><tt>");
            t.println("<h3>Car Dispatch Summary</h3>");
            String sql_carInfo = "SELECT car_details.Car_Size, car_details.Bonus_Hours, "
                    + "car_logs.Arrival_Date, car_logs.Arrival_Time, car_logs.Amount, car_logs.Space_Occupied, "
                    + "car_logs.Departure_Date, car_logs.Departure_Time, car_logs.Total_Hours_Spent,"
                    + "car_logs.Hourly_Charge_Fee FROM car_details,car_logs "
                    + "WHERE car_logs.Registration_Number=car_details.Registration_Number AND car_details.Registration_Number='"+dispatched_car+"' AND Arrival_Date='"+arrival_date1+"' AND Arrival_Time='"+arrival_time1+"'";
            ResultSet carInfo = stmt.executeQuery(sql_carInfo);
            carInfo.next();
            
            t.println("<div class='row'>");
            t.println("<div class='col-lg-6'>");
            t.println("Registration Number : ");
            t.println("</div>");
            t.println("<div class='col-lg-6'>");
            t.println(dispatched_car);
            t.println("</div>");
            t.println("</div>");
            t.println("<div class='row'>");
            t.println("<div class='col-lg-6'>");
            t.println("Arrival Date & Time: ");
            t.println("</div>");
            t.println("<div class='col-lg-6'>");
            t.println(carInfo.getString("Arrival_Date")+" at "+carInfo.getString("Arrival_Time")+" (24h clock-time)");
            t.println("</div>");
            t.println("</div>");
            t.println("<div class='row'>");
            t.println("<div class='col-lg-6'>");
            t.println("Parking Space used: ");
            t.println("</div>");
            t.println("<div class='col-lg-6'>");
            t.println(carInfo.getString("Space_Occupied"));
            t.println("</div>");
            t.println("</div>");
            t.println("<div class='row'>");
            t.println("<div class='col-lg-6'>");
            t.println("Departure Date & Time: ");
            t.println("</div>");
            t.println("<div class='col-lg-6'>");
            t.println(carInfo.getString("Departure_Date")+" at "+carInfo.getString("Departure_Time")+" (24h clock-time)");
            t.println("</div>");
            t.println("</div>");
            t.println("<div class='row'>");
            t.println("<div class='col-lg-6'>");
            t.println("Hours Spent: ");
            t.println("</div>");
            t.println("<div class='col-lg-6'>");
            t.println(carInfo.getString("Total_Hours_Spent"));
            t.println("</div>");
            t.println("</div>");
            t.println("<div class='row'>");
            t.println("<div class='col-lg-6'>");
            t.println("Outstanding Bonus hours: ");
            t.println("</div>");
            t.println("<div class='col-lg-6'>");
            t.println(carInfo.getString("Bonus_Hours"));
            t.println("</div>");
            t.println("</div>");
            t.println("<div class='row'>");
            t.println("<div class='col-lg-6'>");
            t.println("Charge per hour: ");
            t.println("</div>");
            t.println("<div class='col-lg-6'>");
            t.println(carInfo.getString("Hourly_Charge_Fee"));
            t.println("</div>");
            t.println("</div>");
            t.println("<div class='row'>");
            t.println("<div class='col-lg-6'>");
            t.println("<b>Total Amount(Shs0: </b>");
            t.println("</div>");
            t.println("<div class='col-lg-6'>");
            t.println("<b>"+carInfo.getString("Amount")+"</b>");
            t.println("</div>");
            t.println("</div>");
            t.println("</tt></div>");
            
            
            } catch (SQLException ex) {
                t.println(ex.getMessage());
            }
            
        }
    }
    /*
    
    *Function to handle displaying and operations on parking yards
    */
    private void displayParkingYards(PrintWriter t, HttpServletRequest request)
    {
    /*
        
        *Javascript functions to highlight the parking cell when a mouse hovers on
        * It also displays the cell information when the user clicks on the cell
        
        */    
        String script ="<script>\n" +
"            function highlightCellOnHover(id)\n" +
"            {\n" +
"                var cell = document.getElementById(\n" +
"                        id);\n" +
"                cell.style.background='grey';\n"
                + "cell.style.cursor='pointer';\n" +
"            }\n" +
                "function unHighlightCellOnLeave(id)\n" +
"            {\n" +
"                var cell = document.getElementById(\n" +
"                        id);\n" +
"                cell.style.background='#bba';\n" +
"            }\n"
                + "function redirectPage(id){\n"
                + "document.location='/xyz/CarParkingManager?action=view_spaces&selected_cell_to_view='+id;\n"
                + "\n"
                + "}\n"
                + "function autoSubmitForm(){\n"
                + "var form =document.getElementById(\"park_yard_form\");\n"
                + "form.submit();\n"
                + ""
                + ""
                + "\n}\n"
                + ""+
"        </script>";
        t.print(script);
        String cell_to_view = request.getParameter("selected_cell_to_view");
        
        String parking_yard = request.getParameter("parking_yard_selected");
        /*
        *A session here is used to remember the last parking yard the user selected
        *If a user hasn't selected any parking, the "Maing Grounds" Yard will be displayed
        */
        HttpSession userSession = request.getSession(true);
        //t.println(cell_to_view);
        if(cell_to_view != null)
        {
            try{
            ResultSet cell_chosen = stmt.executeQuery("SELECT  parking_space.Space_ID, parking_space.Space_Status,parking_space.Car_in_space,car_details.Car_Size FROM parking_space,car_details WHERE parking_space.Car_in_Space=car_details.Registration_Number AND Space_ID='"+cell_to_view+"' ");
            
            
            String alertScript,car;
            if(cell_chosen.next()){
                int o_status = cell_chosen.getInt("Space_Status");
                car = cell_chosen.getString("Car_in_Space");
               String car_size = cell_chosen.getString("Car_Size");
            alertScript ="<script type='text/javascript'>\n"
                    + "alert('Cell Identifier: "+cell_to_view+"\\n Occupation State : Occupied\\n Car available : "+car+" \\n Car Size: "+car_size+"' )\n"
                    
                    + "\ndocument.location='/xyz/CarParkingManager?action=view_spaces';\n"
                    + "</script>\n";
            }
            else{
                alertScript ="<script type='text/javascript'>\n"
                    + "alert('Cell Identifier: "+cell_to_view+"\\n Occupation State : Empty\\n Car available : NULL' )\n"
                    
                    + "\ndocument.location='/xyz/CarParkingManager?action=view_spaces';\n"
                    + "</script>\n";
            }
            t.println(alertScript);
            }
            catch(SQLException e)
            {
                t.println(e.getMessage());
            }
        }
        else if(parking_yard == null)
        {
            if(userSession.getAttribute("parking_yard")==null){
             parking_yard = "Main Grounds";
             userSession.setAttribute("parking_yard", parking_yard);
            }
            else{
                parking_yard = userSession.getAttribute("parking_yard").toString();
            }
        }
        else{
            userSession.setAttribute("parking_yard", parking_yard);
        }
         t.println("<div class='row' style='font-size:1.5em; background-color:#ccc; padding:2%;'><div class='col-lg-12'>");
            t.println("<form action='' method='post' id='park_yard_form' >");
            t.println("<label>Choose the parking_yard to view:</label> <select name='parking_yard_selected' onChange='autoSubmitForm();' required='required'> ");
        try {
            ResultSet car_in_parking = stmt.executeQuery("SELECT DISTINCT Name  FROM parking_yard");
            while(car_in_parking.next())
            {
                t.println("<option value='"+car_in_parking.getString(1)+"'>"+car_in_parking.getString(1)+"</option>");
            }
        } catch (SQLException ex) {
            t.println(ex.getMessage());
        }
        t.println("</select>");
        t.println("<p style='align:center;'><input type='submit' name='submit' value='View yard' class='btn btn-primary' style='font-size:1em;'/></p>");
            t.print("</form>");
            t.println("</div></div>");
       
        
        String carId,cellId;
        int var;
        t.println("<div class='panel table-responsive'>");
        t.println("<h1 style='' align='center'>Parking Display For "+userSession.getAttribute("parking_yard")+"</h1>");
        t.println("<table class='table' border=1 style='background:#bba;' width='100%'>");
        t.println("<tr>");
        
        try{
            int status;
            ResultSet count =  stmt.executeQuery("SELECT COUNT(*) FROM parking_space WHERE Yard_Name = '"+parking_yard+"'");
            count.next();
            int number = count.getInt(1);
            
        ResultSet spaces = stmt.executeQuery("SELECT parking_space.Space_ID, parking_space.Space_Status,parking_space.Car_in_space FROM parking_space WHERE Yard_Name = '"+parking_yard+"'");
        
        String[][] cell = new String[number][3];
        int k=0;
        while(spaces.next())
        {
            cell[k][0]=spaces.getString("Space_ID");
            cell[k][1]=spaces.getString("Space_Status");
            cell[k][2]=spaces.getString("Car_in_Space");
            k++;
        }
        
        //t.println(number);
        
        for(int i=0; i<(number/2);i++)
        {
            
            status = Integer.parseInt(cell[i][1]);
            cellId = cell[i][0];
            if(status ==1)
            {
                
                carId = cell[i][2];
                ResultSet s = stmt.executeQuery("SELECT Car_Size FROM car_details WHERE Registration_Number = '"+carId+"'");
                s.next();
                String size = s.getString("Car_Size");
                size = size.toLowerCase();
                t.println("<td id ='"+cellId+"' onMouseOver='highlightCellOnHover(\""+cellId+"\");' onMouseOut='unHighlightCellOnLeave(\""+cellId+"\");'"
                    + " onClick='redirectPage(\""+cellId+"\");'>"
                    + ""+cellId+"<img class='img-responsive' src='images/"+size+".jpg' alt='"+carId+"' width='100%'/>"
                    +carId+"</td>");
                
                
            }
            else{
            
            
            t.println("<td id ='"+cellId+"' onMouseOver='highlightCellOnHover(\""+cellId+"\");' onMouseOut='unHighlightCellOnLeave(\""+cellId+"\");'"
                    + " onClick='redirectPage(\""+cellId+"\");'>"
                    + ""+cellId+"<br/> <br/>Free Space"
                    +"</td>");
            }
        }
        t.println("</tr>");
        t.println("<tr height=100px><td colspan=8>"
                + "<img class='img-responsive' src='images/road.jpg' alt='Parking' width='100%' height='20px'/>"
                + "</td></tr>");
        t.println("<tr>");
        for(int i=(number/2); i<number; i++)
        {
            status = Integer.parseInt(cell[i][1]);
            cellId = cell[i][0];
            if(status ==1)
            {
                
                carId = cell[i][2];
                ResultSet s = stmt.executeQuery("SELECT Car_Size FROM car_details WHERE Registration_Number = '"+carId+"'");
                s.next();
                String size = s.getString("Car_Size");
                size = size.toLowerCase();
                t.println("<td id ='"+cellId+"' onMouseOver='highlightCellOnHover(\""+cellId+"\");' onMouseOut='unHighlightCellOnLeave(\""+cellId+"\");'"
                    + " onClick='redirectPage(\""+cellId+"\");'>"
                    + ""+cellId+"<img class='img-responsive' src='images/"+size+".jpg' alt='"+carId+"' width='100%'/>"
                    +carId+"</td>");
                
                
            }
            else{
            
            
            t.println("<td id ='"+cellId+"' onMouseOver='highlightCellOnHover(\""+cellId+"\");' onMouseOut='unHighlightCellOnLeave(\""+cellId+"\");'"
                    + " onClick='redirectPage(\""+cellId+"\");'>"
                    + ""+cellId+"<br/> <br/>Free Space"
                    +"</td>");
            }
        }
        t.println("</tr></table>");
        }
        
        
        catch(SQLException e)
        {
            t.println(e.getMessage());
        }
        t.println("</div>");
        
        
    
    }
    /**
     * 
     * @param t 
     * 
     * updateDatabaseInfo calculates the hours spent and the amount payable by the car for each of the days before today
     */
     
    private void updateDatabaseInfo(PrintWriter t) {
       
            try{
                
                
                
                Calendar c = new GregorianCalendar();
                //t.println(c.getTime().toString());
                
                String cars_sql ="SELECT DISTINCT Registration_Number FROM car_logs";
                ResultSet rs = stmt.executeQuery("SELECT COUNT(Registration_Number) FROM car_logs");
                rs.next();
                int i= rs.getInt(1);
                String []cars = new String[i];
                ResultSet cars_logged = stmt.executeQuery(cars_sql);
                
                String temp_sql;
                
                
                int j=0;
                
                //t.println(i);
                while(cars_logged.next())
                {
                    cars[j] = cars_logged.getString("Registration_Number");  
                    j++;
                }
                ResultSet record;
                for(String car: cars)
                {
                    temp_sql = "SELECT car_logs.Arrival_Date, car_logs.Arrival_Time,logged,car_logs.space_occupied,car_logs.last_logged_date, car_logs.Hourly_Charge_Fee, car_details.Bonus_Hours FROM car_logs, car_details WHERE car_logs.Registration_Number=car_details.Registration_Number AND car_logs.Registration_Number= '"+car+"' AND car_logs.Departure_Date ='1930-01-01'";
                    
                    record = stmt.executeQuery(temp_sql);
                    if(record.next()){
            //        t.println("after this");
                    int year,month,day,hour,minutes,seconds;
                    boolean first_time_log=false;
                    float free_hours = record.getFloat("Bonus_hours");
                    String arrival_time=record.getString("Arrival_Time");
                    String arrival_date = record.getString("Arrival_Date");
                    float charge_fee = record.getFloat("Hourly_Charge_Fee");
                    if((record.getString("Arrival_Date").equals(record.getString("last_logged_date")) && (record.getInt("logged")==0))){
                    String []date = record.getString("Arrival_Date").split("-");
                    year = Integer.parseInt(date[0]);
                    month= Integer.parseInt(date[1])-1;
                    day = Integer.parseInt(date[2]);
                    String []time = record.getString("Arrival_Time").split(":");
                    hour = Integer.parseInt(time[0]);
                    minutes = Integer.parseInt(time[1]);
                    seconds = Integer.parseInt(time[2]);
                    first_time_log=true;
                   // t.println(year +":"+month+":"+day);
                    }
                    else{
                            String []date = record.getString("last_logged_date").split("-");
                        year = Integer.parseInt(date[0]);
                        month= Integer.parseInt(date[1])-1;
                        day = Integer.parseInt(date[2]);
                        //String []time = record.getString("Arrival_Time").split(":");
                        hour = 0;
                        minutes = 0;
                        seconds = 0;
                    }
                    String cell= record.getString("space_occupied");
                    Calendar x = new GregorianCalendar(year, month,day, hour,minutes, seconds);
                    Calendar today = new GregorianCalendar();
                    if(!first_time_log)
                    {
                        x.add(Calendar.DAY_OF_MONTH, 1);
                    }
                    today.set(Calendar.HOUR_OF_DAY,0);
                    today.set(Calendar.MINUTE,0);
                    today.set(Calendar.SECOND,0);
                    
                   //x.add(Calendar.DAY_OF_MONTH, 1);
                    int k=0;
                    float hours,cost_per_day,hours_not_for_charge;
                    long timeStamp1, timeStamp2;
                    //t.print(today.get(Calendar.HOUR));
                   // t.println("<table class='table table-bordered' width=50%>");
                    String x_date, today_date,sql_to_update_daily_logs,last_logged_date="";
                    today_date=today.get(Calendar.YEAR)+"-"+(today.get(Calendar.MONTH)+1)+"-"+today.get(Calendar.DAY_OF_MONTH);
                    x_date = x.get(Calendar.YEAR)+"-"+(x.get(Calendar.MONTH)+1)+"-"+x.get(Calendar.DAY_OF_MONTH);
                    while(today.compareTo(x)>0 && !x_date.equals(today_date))
                    {
                        if(k==0 &&first_time_log)
                        {
                            Calendar first_day = new GregorianCalendar(x.get(Calendar.YEAR),x.get(Calendar.MONTH),x.get(Calendar.DAY_OF_MONTH),x.get(Calendar.HOUR),x.get(Calendar.MINUTE),x.get(Calendar.SECOND));
                            first_day.set(Calendar.HOUR_OF_DAY,23);
                            first_day.set(Calendar.MINUTE,59);
                            first_day.set(Calendar.SECOND,59);
                            
                            timeStamp1 = first_day.getTimeInMillis();
                            timeStamp2 = x.getTimeInMillis();
                            //t.println(x.get(Calendar.HOUR) +"  "+ first_day.get(Calendar.HOUR));
                            hours = (float)((timeStamp1-timeStamp2)/3600000.00);
                            hours_not_for_charge=hours;
                            if(hours>=free_hours && free_hours!=0)
                            {
                                hours=hours-free_hours;
                                free_hours=0;
                            }
                            else if(free_hours>0){
                                
                                
                                free_hours-=hours;
                                hours =0;
                            }
                            
                            if(hours>9)
                        {
                            cost_per_day = (float)0.89*hours*charge_fee;
                        }
                            else{cost_per_day = (float)hours*charge_fee;}
                     //       t.println("<tr> <td> "+x.getTime().toString()+"</td> <td>"+hours+"</td> <td>"+cost_per_day+"</td></tr>");
                           sql_to_update_daily_logs = "INSERT INTO daily_logs(Registration_Number,Log_Day, Charge_Fee,Space_Occupied,Hours_Spent_Today,Total_Hours_Spent_Today) VALUES('"+car+"','"+x_date+"','"+cost_per_day+"','"+cell+"','"+hours+"','"+hours_not_for_charge+"')";
                            stmt.execute(sql_to_update_daily_logs );
                            last_logged_date = x_date;
//t.println(sql_to_update_daily_logs);                          
                            x.add(Calendar.DAY_OF_MONTH, 1);
                            x.set(Calendar.HOUR_OF_DAY,0);
                            x.set(Calendar.MINUTE,0);
                            x.set(Calendar.SECOND,0);
                            
                        }
                        else{
                            
                            Calendar next_day = new GregorianCalendar(x.get(Calendar.YEAR),x.get(Calendar.MONTH),x.get(Calendar.DAY_OF_MONTH),x.get(Calendar.HOUR),x.get(Calendar.MINUTE),x.get(Calendar.SECOND));
                            next_day.add(Calendar.DAY_OF_MONTH, 1);
                            
                            timeStamp1 = next_day.getTimeInMillis();
                            timeStamp2 = x.getTimeInMillis();
                            
                            hours = (float)((timeStamp1-timeStamp2)/3600000.00);
                            hours_not_for_charge=hours;
                                if(hours>=free_hours && free_hours!=0)
                                {
                                    hours=hours-free_hours;
                                    free_hours=0;
                                }
                                else if(free_hours>0){


                                    free_hours-=hours;
                                    hours =0;
                                }
                            if(hours>9)
                            {
                              cost_per_day = (float)0.89*hours*charge_fee;
                            }
                            else{cost_per_day = (float)hours*charge_fee;}
                            
                            sql_to_update_daily_logs = "INSERT INTO daily_logs(Registration_Number,Log_Day, Charge_Fee,Space_Occupied,Hours_Spent_Today,Total_Hours_Spent_Today) VALUES('"+car+"','"+x_date+"','"+cost_per_day+"','"+cell+"','"+hours+"','"+hours_not_for_charge+"')";
                            stmt.execute(sql_to_update_daily_logs);
                            last_logged_date = x_date;
                       //     t.println("<tr> <td> "+x.getTime().toString()+"</td> <td>"+hours+"</td> <td>"+cost_per_day+"</td></tr>");
                            x.add(Calendar.DAY_OF_MONTH, 1);
                        }
                        x_date = x.get(Calendar.YEAR)+"-"+(x.get(Calendar.MONTH)+1)+"-"+x.get(Calendar.DAY_OF_MONTH);
                        k++;
                        
                        
                    }
                    if(!last_logged_date.isEmpty()){     
                    stmt.execute("UPDATE car_logs SET last_logged_date='"+last_logged_date+"',logged='1'  WHERE Registration_Number= '"+car+"' AND Arrival_Date ='"+arrival_date+"' AND Arrival_Time='"+arrival_time+"'");
                    }  
                    
                    
                        stmt.execute("UPDATE car_details SET Bonus_Hours='"+free_hours+"'  WHERE Registration_Number= '"+car+"'");      
                    
                    
                }
                    else{continue;}
                }
            
                
                
            }
            catch(SQLException e)
            {
                t.println(e.toString());
            }
    }
    /*
    handleCarAssignment is responsible for enabling the user add a new car by choosing the parking cell to allocate
    It then inserts the car in the database and displays a slip with the information that can be used to locate the car
    */
    public void handleCarAssignment(PrintWriter tell, HttpServletRequest request){
        
           
            try{
                String new_car_submitted = request.getParameter("new_car_submitted");
                
                if(new_car_submitted==null){
                 String registration1 = "<div style='font-size:1.5em; padding:2%; background-color:#abd;' class='row' >"
                         + "<h3>Fill the form below to register a new car</h3>"
                                 +"<form action='' for='form' method='post' >"
                       
                                 +"<label>Car Registration Number: </label> <input type='text' name='registration_number' id='registration_number' required/><br></br>"
                                 +"<label>Choose the Car Size: </label><select name='car_size' id='car_size' required><option>Small</option><option>Medium</option><option>Large</option></select><br></br>"
                                 +"<label>Choose the the parking yard: </label>";
                
                                 String registration2 ="<label>Parking Fee per Hour: </label><input type='text' name='parking_hour' id='parking_hour' required/><br></br>"
                                 +"<center>"
                                         + "&nbsp;&nbsp;&nbsp;&nbsp;<input type='submit' name='submit' value='submit' class='btn btn-primary' style='font-size:1.2em;'/>"
                                         + ""
                                         + "&nbsp;&nbsp;&nbsp;&nbsp;<input type='reset' name='submit' value='Clear' class='btn btn-warning' style='font-size:1.2em;'/>"
                                         + "</center>"
                                 +"  <input type='hidden' name='new_car_submitted' value='new_car_submitted'/></form></div>";
                 tell.println(registration1);
                   
                 ResultSet yardsCount = stmt.executeQuery("SELECT COUNT(*) FROM parking_yard");
                 yardsCount.next();
                 int yards_num = yardsCount.getInt(1);
                 String []yards_array = new String[yards_num];
                 ResultSet yards = stmt.executeQuery("SELECT * FROM parking_yard");
                 int k=0;
                 String yards_select = "<select name='yard_choice' id='yard_choice' onChange='changeCell();'><option><optio>";
                 while(yards.next())
                 {
                     yards_array[k]=yards.getString("Name");
                     yards_select+="<option value='"+yards_array[k]+"'>"+yards_array[k]+"</option>";
                     k++;
                 }
                 yards_select+="</select>";
                 
                 String script_array="<script>\n"
                         + "var yards = new Array();\n";
                 for(String yard : yards_array){
                    script_array += "yards[\""+yard+"\"] = new Array(";
                    String pick="SELECT Space_ID, Yard_Name FROM parking_space WHERE Space_Status='0' AND Yard_Name='"+yard+"';";
                    ResultSet query=stmt.executeQuery(pick);
                    while(query.next())
                    {
                        script_array+="\""+query.getString("Space_ID")+"\",";
                    }
                    script_array=script_array.substring(0,script_array.length()-1)+");\n";
                 
                 }
                 script_array+="function changeCell(){\n"
                         + "var val = document.getElementById('yard_choice').value;\n"
                         + "var cell = document.getElementById(\"cells_available\");\n"
                         + ""
                         + "var option;\n"
                         + "var opt;\n"
                         + "var num = yards[val].length;"
                         + "var k =cell.options.length;"
                         + "for(var i=0; i< k; i++){\n"
                         + "cell.remove(0);"
                         + ""
                         + "\n}"
                         + ""
                         + "for(var i=0; i<num; i++){\n"
                         + "opt =yards[val][i];\n "
                         + " "
                         + "option = new Option(opt,opt);\n"
                         + ""
                         + "cell.add(option,undefined);"
                         + "\n}"
                         
                         + "}\n";
                 script_array+="</script>";
                 tell.println(script_array);
                 tell.println(yards_select+"<br /><br />");
                 tell.println("<label>Choose the parking Cell </label> <select name='cells_available' id='cells_available'>");
                 tell.println("</select><br></br>");
                 tell.println(registration2);
                     
             }
                else{
                
               String reg = request.getParameter("registration_number");
               String size = request.getParameter("car_size");
               String space = request.getParameter("cells_available");
               String fee1 = request.getParameter("parking_hour");
               float fee=Float.parseFloat(fee1);
               Calendar p = new GregorianCalendar();
               //to check whether the car is already in parking
               ResultSet rst2 = stmt.executeQuery("SELECT Space_Occupied FROM car_logs WHERE Registration_Number='"+reg+"' AND Departure_Date='1930-01-01'");
               
               if(rst2.next())
               {
                   String script="<script>\n"
                           + "alert('The car "+reg+" you have added is already in parking.');\n"
                           + "document.location='/xyz/CarParkingManager?action=view_spaces'"
                           + "\n</script>";
                   tell.println(script);
               }
               else{
               
               //Date hour=p.getTime();
               
               int year = p.get(Calendar.YEAR);
               int month =p.get(Calendar.MONTH)+1;
               int day = p.get(Calendar.DAY_OF_MONTH);
               int hour = p.get(Calendar.HOUR_OF_DAY);
               int min = p.get(Calendar.MINUTE);
               int sec = p.get(Calendar.SECOND);
               
               
               
               String arrival_date = year+"-"+month+"-"+day;
               String arrival_time = hour+":"+min+":"+sec;
                 ResultSet checkCar = stmt.executeQuery("SELECT COUNT(*) FROM car_details WHERE Registration_Number='"+reg+"'");
                 checkCar.next();
                 if(checkCar.getInt(1)==0){
                 String query1="INSERT INTO car_details(Registration_Number, Car_Size) VALUES('"+reg+"','"+size+"')";
                 boolean rset=stmt.execute(query1);
                 }
                 String query2="INSERT INTO car_logs(Registration_Number, Arrival_date, Last_Logged_Date, Arrival_Time, Space_Occupied,Hourly_Charge_Fee) VALUES('"+reg+"','"+arrival_date+"','"+arrival_date+"', '"+arrival_time+"','"+space+"','"+fee+"')";
                 //tell.println(query2);
                 boolean rset1=stmt.execute(query2);
                 
                 
                           
                 
                 String query3="UPDATE parking_space SET Space_Status='1', CAR_IN_SPACE='"+reg+"' WHERE Space_ID='"+space+"'";
                 //tell.println(query3);
                 int rset2=stmt.executeUpdate(query3);
                 
                 tell.println("<h3>Car Registration Information</h3>");
            ResultSet rst1 = stmt.executeQuery("SELECT Bonus_Hours FROM car_details WHERE Registration_Number='"+reg+"' ");
             float free_hours ;
            if(rst1.next())
            {
                free_hours = rst1.getFloat("Bonus_Hours");
            }
            else{
                free_hours = 0;
            }
            
            ResultSet rst4 = stmt.executeQuery("SELECT Yard_Name from parking_space WHERE Space_ID='"+space+"'");
            rst4.next();
            
            tell.println("<div class='row'>");
            tell.println("<div class='col-lg-6'>");
            tell.println("Registration Number : ");
            tell.println("</div>");
            tell.println("<div class='col-lg-6'>");
            tell.println(reg);
            tell.println("</div>");
            tell.println("</div>");
            tell.println("<div class='row'>");
            tell.println("<div class='col-lg-6'>");
            tell.println("Arrival Date & Time: ");
            tell.println("</div>");
            tell.println("<div class='col-lg-6'>");
            tell.println(arrival_date+" at "+arrival_time+" (24h clock-time)");
            tell.println("</div>");
            tell.println("</div>");
            tell.println("<div class='row'>");
            tell.println("<div class='col-lg-6'>");
            tell.println("Parking Space assigned: ");
            tell.println("</div>");
            tell.println("<div class='col-lg-6'>");
            tell.println(space + " in "+rst4.getString("Yard_Name")+" parking yard");
            tell.println("</div>");
            tell.println("</div>");
            tell.println("<div class='row'>");
            tell.println("<div class='col-lg-6'>");
            tell.println("Outstanding Bonus hours: ");
            tell.println("</div>");
            tell.println("<div class='col-lg-6'>");
            tell.println(free_hours);
            tell.println("</div>");
            tell.println("</div>");
            tell.println("<div class='row'>");
            tell.println("<div class='col-lg-6'>");
            tell.println("Charge per hour: ");
            tell.println("</div>");
            tell.println("<div class='col-lg-6'>");
            tell.println(fee);
            tell.println("</div>");
            tell.println("</div>");
            
            tell.println("</tt></div>");
               }
                 
                }

            
            }catch(SQLException ex){
                String message=ex.getMessage();
                tell.println(message);
                        
                 }
             }
    /*
        *computeFreeHours determines the amount of free hours to be given to the car if it has spent more that 30% of 168 hours in 7 consecutive days
        
    */
    private void computeFreeHours(PrintWriter t)
    {
        try{
            
        String cars_sql ="SELECT * FROM car_details WHERE Bonus_Hours=0.0";
        
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM car_details WHERE Bonus_Hours='0'");
        
        rs.next();
        int i = rs.getInt(1);
        //int i=2;
        String [][] cars_returned = new String[i][2];
        
       ResultSet cars = stmt.executeQuery(cars_sql);
       //t.print("After here");
       int j=0;
       while(cars.next()){
           cars_returned[j][0]=cars.getString("Registration_Number");
           cars_returned[j][1]=cars.getString("Date_Of_Bonus_Hours");
           j++;
       }
       
       for(String[] theCar : cars_returned)
       {
           
           if(theCar[1].equals("1930-01-01"))
           {   
               String sql = "SELECT Log_Day, Hours_Spent_Today FROM daily_logs WHERE Registration_Number ='"+theCar[0]+"' ORDER BY Log_Day ASC";
              // t.println(sql);
               ResultSet logs = stmt.executeQuery(sql);
               boolean first_day =true;
               Calendar day1 = new GregorianCalendar(1900,01,01);
               Calendar x_day = new GregorianCalendar(1900,01,01);
               String []day_data;
               float sum=0;
               int day=1;
               while(logs.next() && (first_day || x_day.before(day1)))
               {
                   if(day>7){
                       break;
                   }
                   else{
                       sum+=Float.parseFloat(logs.getString("Hours_Spent_Today"));
                    }
                   if(day==1)
                   {
                       day_data = logs.getString("Log_Day").split("-");
                       day1.set(Calendar.YEAR, Integer.parseInt(day_data[0]));
                       day1.set(Calendar.MONTH, Integer.parseInt(day_data[1])-1);
                       day1.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day_data[2]));
                       day1.add(Calendar.DAY_OF_MONTH,7);
                       
                   }
                   day_data = logs.getString("Log_Day").split("-");
                   x_day.set(Calendar.YEAR, Integer.parseInt(day_data[0]));
                   x_day.set(Calendar.MONTH, Integer.parseInt(day_data[1])-1);
                   x_day.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day_data[2]));
                   
                   //t.println("<p>Log_Day: "+logs.getString("Log_Day")+" Hours: "+logs.getString("Hours_Spent_Today")+"</p>");
                   day++;
                   first_day=false;
               }
               
               float free_hours;
                      if(day==8){
                       free_hours= sum>(0.3*7*24)?(float)(sum*0.1):0;
                       if(free_hours>0)
                            {
                                Calendar today = new GregorianCalendar();

                                String date_today = today.get(Calendar.YEAR)+"-"+(today.get(Calendar.MONTH)+1)+"-"+today.get(Calendar.DAY_OF_MONTH);
                                String sql_set_free_hours = "UPDATE car_details SET Bonus_Hours='"+free_hours+"', Date_Of_Bonus_Hours='"+date_today+"' WHERE Registration_Number = '"+theCar[0]+"'";

                                //t.println(sql_set_free_hours);
                                stmt.execute(sql_set_free_hours);

                                //t.println("<p>"+theCar[0]+ " : "+sum+" "+free_hours+"</p>");
                            }
                      }
                      else{
                          //do nothing because the car has not accumulated 7 consecutive days
                      }
               
               
              
           }  
           else{
               String sql = "SELECT Log_Day, Hours_Spent_Today FROM daily_logs WHERE Registration_Number ='"+theCar[0]+"' AND Log_Day>'"+theCar[1]+"' ORDER BY Log_Day ASC";
               //t.println(sql);
               ResultSet logs = stmt.executeQuery(sql);
               boolean first_day =true;
               Calendar day1 = new GregorianCalendar(1900,01,01);
               Calendar x_day = new GregorianCalendar(1900,01,01);
               String []day_data;
               float sum=0;
               int day=1;
               while(logs.next() && (first_day || x_day.before(day1)))
               {
                 if(day>7){
                       break;
                   }
                   else{
                       sum+=Float.parseFloat(logs.getString("Hours_Spent_Today"));
                    }
                   if(day==1)
                   {
                       day_data = logs.getString("Log_Day").split("-");
                       day1.set(Calendar.YEAR, Integer.parseInt(day_data[0]));
                       day1.set(Calendar.MONTH, Integer.parseInt(day_data[1])-1);
                       day1.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day_data[2]));
                       day1.add(Calendar.DAY_OF_MONTH,7);
                       
                   }
                   day_data = logs.getString("Log_Day").split("-");
                   x_day.set(Calendar.YEAR, Integer.parseInt(day_data[0]));
                   x_day.set(Calendar.MONTH, Integer.parseInt(day_data[1])-1);
                   x_day.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day_data[2]));
                   
                   //t.println("<p>Log_Day: "+logs.getString("Log_Day")+" Hours: "+logs.getString("Hours_Spent_Today")+"</p>");
                   day++;
                   first_day=false;
               }
               
               float free_hours;
               if(day==8){
                       free_hours = sum>(0.3*7*24)?(float)(sum*0.1):0;
               
               if(free_hours>0)
               {
                   Calendar today = new GregorianCalendar();
                   
                   String date_today = today.get(Calendar.YEAR)+"-"+(today.get(Calendar.MONTH)+1)+"-"+today.get(Calendar.DAY_OF_MONTH);
                   String sql_set_free_hours = "UPDATE car_details SET Bonus_Hours='"+free_hours+"', Date_Of_Bonus_Hours='"+date_today+"' WHERE Registration_Number = '"+theCar[0]+"'";
                   
                   //t.println(sql_set_free_hours);
                   stmt.execute(sql_set_free_hours);
                   //t.println("<p>"+theCar[0]+ " : "+sum+" "+free_hours+"</p>");
               }
               
               }
               else{
                   //do nothing because 8 days have not passed after the last bonus award date
               }
           }
           
       }
       
        
        }
        catch(SQLException e)
        {
            t.println(e.getMessage());
        }
    }
    
    /*
    *The method viewCarLogs enables the user choose a particular and then it displays the information
     about the car for all the days it has ever spent in parking
    */
    public void viewCarLogs(PrintWriter p, HttpServletRequest request){
    String car = request.getParameter("car_logs");
    p.println("<form action='' method='post' for='form' style='font-size:1.4em;'>");
    p.println("<label>Choose the car log to view:</label> <select name='car_logs' required='required'> ");
    try {
    ResultSet car_log = stmt.executeQuery("SELECT DISTINCT Registration_Number FROM daily_logs ");
    while(car_log.next())
    {
    p.println("<option value='"+car_log.getString(1)+"'>"+car_log.getString(1)+"</option>");
    }
    } catch (SQLException ex) {
    p.println(ex.getMessage());
    }
    p.println("</select>");
    p.println("<input type='submit' name='submit' value='View Log' class='btn btn-primary'/>");
    p.println("</form>");
    
    HttpSession userSession = request.getSession(true);
    if(car==null)
    {
        car = userSession.getAttribute("car_to_view").toString();
    }
    if(car != null){
    
        userSession.setAttribute("car_to_view", car);
    
    try {
    ResultSet car_log = stmt.executeQuery("SELECT daily_logs.Registration_Number, daily_logs.Log_Day, daily_logs.Hours_Spent_Today,daily_logs.Charge_Fee, daily_logs.Space_Occupied, daily_logs.Total_Hours_Spent_Today,parking_space.Yard_Name FROM daily_logs, parking_space WHERE "+
    "Registration_Number='"+car+"' AND parking_space.Space_ID = daily_logs.Space_Occupied ");
    p.println("<table class='table table-striped'>");
    p.println("<thead> ");
    p.println("<tr>");
    p.println("<tr>");
    p.println("<th colspan='11' style='text-align:center;'><h2> PARKING LOGS FOR "+car+" </h2></th>");
    p.println("</tr>");
    p.println("<tr>");
    p.println("<th> Log Date </th>");
    p.println("<th> Chargeable Hours </th>");
    p.println("<th> Total Hours Spent </th>");
    p.println("<th> Charge Fee</th>");
    p.println("<th> Parking Cell</th>");
    p.println("<th> Parking Yard</th>");
    p.println("</tr>");
    p.println("</thead> ");
    p.println("</tbody> ");
    while(car_log.next())
    {
    p.println("<tr>");
    p.println("<td>"+car_log.getString("Log_Day")+"</td>");
    p.println("<td>"+car_log.getString("Hours_Spent_Today")+"</td>");
    p.println("<td>"+car_log.getString("Total_Hours_Spent_Today")+"</td>");p.println("<td>"+car_log.getString("Charge_Fee")+"</td>");
    p.println("<td>"+car_log.getString("Space_Occupied")+"</td>");
    p.println("<td>"+car_log.getString("Yard_Name")+"</td>");
    p.println("</tr>");
    }
    p.println("</tbody> ");
    p.println("</table> ");
    } catch (SQLException ex) {
    p.println(ex.getMessage());
    }
    }
}
    /*
        *displayCars displays information for all the cars currently in parking showing how much they would 
         pay if they were released now
    */
    public void displayCars(PrintWriter out){
        try{
            
         String displayRows="SELECT car_logs.Registration_Number, car_logs.Arrival_Date, car_logs.Arrival_Time,"
                 + " car_logs.Space_Occupied, car_logs.Hourly_Charge_Fee, car_logs.Departure_Date"
                 + "  FROM car_logs WHERE"
                 + " car_logs.Departure_Date='1930-01-01'";
         
         ResultSet rset3 = stmt.executeQuery("SELECT COUNT(*) FROM car_logs WHERE "
                 + " car_logs.Departure_Date='1930-01-01'");
         rset3.next();
         String [][]cars_in_parking = new String[rset3.getInt(1)][12];
         
         ResultSet rset4=stmt.executeQuery(displayRows);
         int k=0;
         
         while(rset4.next())
         {
              cars_in_parking[k][0]=rset4.getString("Registration_Number");
              cars_in_parking[k][1]=rset4.getString("Arrival_Date");
              cars_in_parking[k][2]=rset4.getString("Arrival_Time");
              cars_in_parking[k][3]=rset4.getString("Space_Occupied");
             // cars_in_parking[k][4]=rset4.getString("cars_in_parking");
              cars_in_parking[k][4]=rset4.getString("Hourly_Charge_Fee");
              cars_in_parking[k][5]=rset4.getString("Departure_Date");
              //cars_in_parking[k][7]=rset4.getString("Space_ID");
              //cars_in_parking[k][8]=rset4.getString("Car_In_Space");
              //cars_in_parking[k][9]=rset4.getString("Space_Status");
              k++;
         }
         
         for(String []car : cars_in_parking)
         {
                    Calendar now = new GregorianCalendar();
                    Calendar start_of_today = new GregorianCalendar();
                    String month = String.valueOf(start_of_today.get(Calendar.MONTH)+1);
                    if(month.length()<2)
                     {
                          month="0"+month;
                        }
                   String x_date =start_of_today.get(Calendar.YEAR)+"-"+
                   month+"-"+
                   start_of_today.get(Calendar.DAY_OF_MONTH);
                  
                   if(x_date.equals(car[1])){
                        String [] time = car[2].split(":");
                        start_of_today.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
                        start_of_today.set(Calendar.MINUTE, Integer.parseInt(time[1]));
                        start_of_today.set(Calendar.SECOND, Integer.parseInt(time[2]));
                   }
                  else{
                        start_of_today.set(Calendar.HOUR_OF_DAY, 0);
                        start_of_today.set(Calendar.MINUTE, 0);
                        start_of_today.set(Calendar.SECOND, 0);
                    }
            
                   float hours_covered_today = (float)((now.getTimeInMillis()-start_of_today.getTimeInMillis())/3600000.00);
                   
                
                   
                  
                   float total_charge =0;
                   float total_hours =0;
                  
           
            String x_time =now.get(Calendar.HOUR_OF_DAY)+":"+
            now.get(Calendar.MINUTE)+":"+
            now.get(Calendar.SECOND);
            float hours = hours_covered_today;
           // out.println(car[0]);
            ResultSet rs1= stmt.executeQuery("SELECT Bonus_Hours FROM car_details WHERE Registration_Number='"+car[0]+"'");
            if(!rs1.next())
            {
                continue;
            }
            float free_hours = rs1.getFloat("Bonus_Hours");
            car[8]=""+free_hours;
            
            if(hours_covered_today>=free_hours && free_hours !=0)
            {
                hours_covered_today-=free_hours;
                free_hours=0;
                //stmt.execute("UPDATE car_details SET Bonus_Hours='"+free_hours+"'  WHERE Registration_Number= '"+dispatched_car+"'");
            }
            else if(free_hours>0)
            {
                free_hours-=hours_covered_today;
                hours_covered_today=0;
                //stmt.execute("UPDATE car_details SET Bonus_Hours='"+free_hours+"'  WHERE Registration_Number= '"+dispatched_car+"'");
            }
             ResultSet amount;
            
                amount = stmt.executeQuery("SELECT Hourly_Charge_Fee,Space_Occupied, Arrival_Date FROM car_logs WHERE Registration_Number='"+car[0]+"' AND Departure_Date='1930-01-01'");
            
            amount.next();
            
            String cell = amount.getString("Space_Occupied");
            String arrival_date = amount.getString("Arrival_Date");
            float charge = hours_covered_today>9?(float)(amount.getFloat(1)*0.89*hours_covered_today):(float)(amount.getFloat(1)*hours_covered_today);
         
            total_charge +=charge;
            total_hours+=hours;
             ResultSet rs = stmt.executeQuery("SELECT daily_logs.Charge_Fee, "
                           + "daily_logs.Total_Hours_Spent_Today, daily_logs.Registration_Number "
                           + "FROM daily_logs WHERE daily_logs.Log_Day>='"+arrival_date+"' "
                           + "AND daily_logs.Registration_Number='"+car[0]+"' AND Space_Occupied='"+car[3]+"'");
             
           while(rs.next())
            {
                total_charge+=rs.getFloat("Charge_Fee");
                total_hours+=rs.getFloat("Total_Hours_Spent_Today");
            } 
            
           car[6]=""+total_hours;
           car[7]=""+total_charge;
           
           ResultSet yard = stmt.executeQuery("SELECT Yard_Name FROM parking_space WHERE SPace_ID='"+car[3]+"'");
           if(yard.next())
           {
               car[9]=yard.getString(1);
           }
         }
         
         
      
        out.println("<h2 class='text text-info' style='text-align:center;'>Cars currently in parking</h2>");
        out.println("<table border=1 class='table table-bordered table-responsives'>"
                    +"<tr style='background-color:#ecc'><th>Registration Number</th><th>Arrival Date</th><th>Arrival Time</th><th>Space Occupied</th>"
                + "<th>Parking Yard</th>"
                + "<th>Charge Per Hour</th><th>Hours Spent</th>"
                + "<th>Bonus Hours</th><th>Amount Payable(shs)</th><th>Remove Car</th></tr>");
        for(String []car:cars_in_parking){


            String RegNo=car[0];
            String Arrive_Date=car[1];
            String Arrive_Time=car[2];
            String Space_Occ=car[3];
            String Hour_Charge=car[4];
            String total_hours_spent =  car[6];
            String amount_payable  =car[7];
            String free_hours =" "+car[8]+"";
            String remove_form ="<form action='/xyz/CarParkingManager?action=release_car' method='post'>"
                    + "<input type='hidden' value='"+RegNo+"' name='dispatch_car'/>"
                    + "<input type='submit' value='Remove '/>"
                    + "</form>";
            out.println("<tr><td>"+RegNo+"</td><td>"+Arrive_Date+"</td><td>"+Arrive_Time+"</td><td>"
                    + ""+Space_Occ+"</td>"
                    + "<td>"+car[9]+"</td>"
                    + "<td>"+Hour_Charge+"</td><td>"+total_hours_spent+"</td>"
                    + "<td>"+free_hours+"</td>"
                    + "<td>"+amount_payable+"</td>"
                    + "<td>"+remove_form+"</td></tr>");
        }
        out.println("</table>"); 
        out.println("<p class='text text-info'>Values for amount payable and hours spent vary with time. They will be diferent on refresh</p>");
             
           }catch(SQLException ex)
                 {
                     out.println(ex.getMessage());
                 }
                                    
         
    }
    /*
    *displayCells gives a tabular display of the parking cells showing how much amount the cell has collected over a particular period of time
    *The user can enter the date range to study
    *by default it displays the last 30 days
    *It uses sessions to remember the date ranges the user previously selected
    */
    public void displayCells(PrintWriter t, HttpServletRequest request){

        //form to enable the user select the range of dates they wish to study
        String form="<div class='row' style='background-color:#ccc; padding:2%;'> <div class='col-lg-12 panel pane-default'><form action='' method='post'>"
                   +"<p><label>Enter the date range to study</label><p>"
                   +"<p><label> Start Date</label> <input type='date' name='date_field1' /> "
                   +"<label> End Date</label> <input type='date' name='date_field2'/></p>"
                + "<p class='help-block'>Date format: yyyy-mm-dd forexample 2016-03-16</p>"
                   +"<input type='hidden' name='hidden_form'/>"
                   + "<input type='submit' value='submit'/>"
                   +"</form></div></div>";
        t.println(form);
        
        //picking the dates entered by the user
        String date_y=request.getParameter("date_field1");
        String date_z=request.getParameter("date_field2");
        
        if(date_y==null && date_z==null)
        {
            /*if the user has not selected, date_z will be set to the current date and date_y be set to the date 30 days ago*/
            
            Calendar today = new GregorianCalendar();
            
            //formatting the date to the format yyyy-mm-dd
            String month = String.valueOf(today.get(Calendar.MONTH)+1);
            if(month.length()<2)
            {
                month = "0"+month;
            }
            date_z = today.get(Calendar.YEAR)+"-"+month+"-"+today.get(Calendar.DAY_OF_MONTH);
            
            Calendar date2 = new GregorianCalendar();
            date2.add(Calendar.DAY_OF_MONTH,-30);
            
            month = String.valueOf(date2.get(Calendar.MONTH)+1);
            if(month.length()<2)
            {
                month = "0"+month;
            }
            
            date_y=date2.get(Calendar.YEAR)+"-"+month+"-"+date2.get(Calendar.DAY_OF_MONTH);
        }
        
       
    try {
//determining the number of parking cells in the database
            ResultSet counter = stmt.executeQuery("SELECT COUNT(*) FROM parking_space");
           counter.next();
           int number_of_cells = counter.getInt(1);
/*
creating a two-dimensional array to store the cells with the corresponding parking-yard where they belong 
           and the amount accumulated over the specified period of time
*/

           String [][] parkingCells= new String[number_of_cells][3];
/*query statement to pick the parking cell from the database along with the name of their corresponding parking yard*/
           ResultSet pick = stmt.executeQuery("SELECT Space_ID, Yard_Name FROM parking_space");
           int k=0;
/* looping through the resultset and string the Cell ID and the Yard Name into the array, the value for the amount in the 
           array is not set yet*/
           while(pick.next()){
               parkingCells[k][0]=pick.getString("Space_ID");
               parkingCells[k][1]=pick.getString("Yard_Name");
               k++;
           }
           /*
                for each of the cell in the array we select the the fees the different cars generated on various days and add them out
                The total amount is stored in index 2 of the cell in the parkingCells array
           */
           for(String []cell : parkingCells){
               ResultSet display = stmt.executeQuery("SELECT Registration_Number, Charge_Fee, Space_Occupied, Log_Day FROM daily_logs WHERE Log_Day BETWEEN '"+date_y+"' AND '"+date_z+"' AND Space_Occupied='"+cell[0]+"'");          
                float amount =0;
                while(display.next()){
                    amount+=display.getFloat(2);
                   }
                cell[2]=""+amount;       
           }

   //Bubble sort algorithm to arrange the cells stored in the parkingCells array in descending order

           String temp_id, temp_space,temp_amount;

           for(int i=0; i<parkingCells.length; i++)
           {
               for(int j=0; j<parkingCells.length-1; j++)
               {
                   if(Float.parseFloat(parkingCells[j][2])<Float.parseFloat(parkingCells[j+1][2]))
                   {
                       temp_id = parkingCells[j][0];
                       temp_space =parkingCells[j][1];
                       temp_amount =parkingCells[j][2];
                       parkingCells[j][0]=parkingCells[j+1][0];
                       parkingCells[j][1]=parkingCells[j+1][1];
                       parkingCells[j][2]=parkingCells[j+1][2];
                       parkingCells[j+1][0]=temp_id;
                       parkingCells[j+1][1]=temp_space;
                       parkingCells[j+1][2]=temp_amount;
                   }

                   
               }
           }


//Displaying the values for each cell in the table with the cells with highest totals on top
           t.println("<h3>Displaying Parking Cells with the totals accumulated during the time between "+date_y+ " to "+date_z+"</h3>");
           t.println("<table border=1 class='table table-bordered'>"
                   +"<tr style='background-color:#ecc'><th>Cell Number</th><th>Parking Yard</th><th>Amount Accumulated(Shs)</th>");

          for(String []cell : parkingCells){

              t.println("<tr><td>"+cell[0]+"</td>"+" "+"<td>"+cell[1]+"</td>"+" "+"<td>"+cell[2]+"</td></tr>" );
          }
        t.println("</table>");
    } catch (SQLException ex) {
        t.println(ex.getMessage());
    }
       
    }
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
