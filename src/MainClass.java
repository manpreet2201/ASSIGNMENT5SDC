
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

//For db.cs.dal.ca, you must be running under Dal's VPN software

public class MainClass {

	// Open a connection to a database, submit a query for the contents of a table,
	// and
	// print the output.
	public static void main(String[] args) {
		// Variables for the connections and the queries.
		Connection connect = null; // the link to the database
		Statement statement = null; // a place to build up an SQL query
		ResultSet resultSet = null;
		Statement statement1 = null;
		Statement statement2 = null;
		// a data structure to receive results from an SQL query
		// Info used for the connection that you will definitely want to change, make
		// into parameters, or draw from environment variables instead of having
		// hard-coded

		Properties identity = new Properties(); // Using a properties structure, just to hide info from other users.
		MyIdentity me = new MyIdentity(); // My own class to identify my credentials. Ideally load Properties from a
											// file instead and this class disappears.

		// final String xmlFilePath = "C:\\Users\\User\\Desktop\\xml1.xml";

		String user;
		String password;
		String dbName;

		Scanner sc = new Scanner(System.in); // System.in is a standard input stream
		System.out.print("Enter start date yyyy-mm-dd: ");
		boolean isDate = false;
		boolean isDate1 = false;
		
		//enter start date 
		String start_date1 = sc.next(); // reads string
		String datePattern = "^\\d{4}-\\d{2}-\\d{2}$";//to check format of date 

		isDate = start_date1.matches(datePattern);
		while (isDate == false) {
			System.out.println("enter start date in yyyy-mm-dd");
			start_date1 = sc.next();
			isDate = start_date1.matches(datePattern);

		}
		// System.out.println("Date :"+ start_date1+": matches with the this date
		// Pattern:"+datePattern+"Ans:"+isDate);
		System.out.print("Enter end date in yyyy-mm-dd: ");
		//enter end date
		String end_date1 = sc.next();
		isDate1 = end_date1.matches(datePattern);

		while (isDate1 == false) {
			System.out.println("enter end date in yyyy-mm-dd");
			end_date1 = sc.next();
			isDate1 = end_date1.matches(datePattern);//checking fomat of date

		}
		System.out.println("Enter the path of file:");
		String path = sc.next();

		// This query gives the customer name, address, number of orders in the period,
		// and total
		// order value
		String query = "with orderWithDetails as " + "(select orders.CustomerID,SUM(Quantity*UnitPrice) as total "
				+ ",orders.OrderID,orders.OrderDate from orders "
				+ "INNER JOIN orderdetails ON orders.OrderID=orderdetails.OrderID " + "AND orders.OrderDate between '"
				+ start_date1 + "' and '" + end_date1 + "' "
				+ "group by orders.OrderID) select  distinct customers.CustomerID,ContactName"
				+ " as customer_name ,Address as street_address,City,Region,PostalCode,Country,"
				+ " Count(orderWithDetails.OrderID) as num_orders,SUM(orderWithDetails.total)"
				+ " as product_value from customers,orderWithDetails  where "
				+ "customers.CustomerID=orderWithDetails.CustomerID AND " + "orderWithDetails.OrderDate between '"
				+ start_date1 + "' and ' " + end_date1 + " ' " + "group by customers.CustomerID";

		// This query for each product category, the category name and for each product
		// in
		// the category report the name, supplier, units sold, and value of product sold

		String query1 = "select categories.CategoryName,pro.ProductID,pro.ProductName,sup.CompanyName,sum(Quantity) as units_sold,sum(Quantity)* UnitPrice as value_of_product "
				+ "from orders  natural join orderdetails   natural join products as pro "
				+ "natural join suppliers as sup natural join categories where orders.OrderDate between  '"
				+ start_date1 + "' and '" + end_date1 + "' " + "group by ProductID";

		// This query gives the supplier name, address, number of products that we sold,
		// and the
		// total value of business that we sold from this supplier’s products.
		String query2 = "with suppinfo as (select products.supplierid, productid, "
				+ "orders.OrderDate,sum(Quantity * UnitPrice) as product_value from "
				+ "products natural join orders natural join orderdetails where OrderDate " + "between '" + start_date1
				+ "' and '" + end_date1 + "' " + " group by productid)select suppliers.CompanyName,"
				+ "suppliers.Address,suppliers.City,suppliers.Region,suppliers.PostalCode,suppliers.Country,"
				+ "count(productid) as"
				+ " num_orders,sum(product_value) as product_value from suppliers natural join suppinfo group by supplierid";

		me.setIdentity(identity); // Fill the properties structure with my info. Ideally, load properties from a
									// file instead to replace this bit.
		user = identity.getProperty("user");
		password = identity.getProperty("password");
		dbName = identity.getProperty("database");

		try {
			// This will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.cj.jdbc.Driver");

			// Setup the connection with the DB
			connect = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306?serverTimezone=UTC&useSSL=false",
					user, password);

			// Statements allow to issue SQL queries to the database. Create an instance
			// that we will use to ultimately send queries to the database.
			statement = connect.createStatement();
			statement1 = connect.createStatement();
			statement2 = connect.createStatement();

			// Choose a database to use
			statement.executeQuery("use " + dbName + ";");
			statement1.executeQuery("use " + dbName + ";");
			statement2.executeQuery("use " + dbName + ";");

			// Result set gets the result of the SQL query
			resultSet = statement.executeQuery(query);

			// Print the output of a resultSet that extracted the employee table.

			// ResultSet is the sequence of returned rows. Its initial position is
			// before the first data set so calling "next"
			// at the start queues up the first answer.
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();//creating document  builder instance

			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

			Document document = documentBuilder.newDocument();//cretaing document

			//Creating the elements using the Element class and its appendChild method.
			Element root = document.createElement("year_end_summary");//root element
			document.appendChild(root);

			Element year = document.createElement("year");//child element of year_end_summary
			root.appendChild(year);

			Element start_date = document.createElement("start_date");//start_date element
			start_date.appendChild(document.createTextNode(start_date1));
			year.appendChild(start_date);

			Element end_date = document.createElement("end_date");
			end_date.appendChild(document.createTextNode(end_date1));
			year.appendChild(end_date);

			Element customer_list = document.createElement("customer_list");//child element of root
			root.appendChild(customer_list);

			Element product_list = document.createElement("product_list");//child element of root
			root.appendChild(product_list);

			Element supplier_list = document.createElement("supplier_list");//child element of root
			root.appendChild(supplier_list);

			//gets the result from first query
			while (resultSet.next()) {

				// It is possible to get the columns via name.
				// It is also possible to get the columns via the column number,
				// which starts at 1.
				// e.g. resultSet.getSTring(2);
				String customer_name1 = (resultSet.getString("customer_name") == null) ? ""
						: resultSet.getString("customer_name");
				String street_address1 = (resultSet.getString("street_address") == null) ? ""
						: resultSet.getString("street_address");
				String city1 = (resultSet.getString("City") == null) ? "" : resultSet.getString("City");

				String postalcode1 = (resultSet.getString("PostalCode") == null) ? ""
						: resultSet.getString("PostalCode");
				String region1 = (resultSet.getString("Region") == null) ? "" : resultSet.getString("Region");
//				String postalcode1 = resultSet.getString("PostalCode");
				String country1 = (resultSet.getString("Country") == null) ? "" : resultSet.getString("Country");
				Integer num_orders1 = resultSet.getInt("num_orders");
				Double product_value = resultSet.getDouble("product_value");
						

				Element customer = document.createElement("customer");
				customer_list.appendChild(customer);

				Element customer_name = document.createElement("customer_name");
				customer_name.appendChild(document.createTextNode(customer_name1));
				customer.appendChild(customer_name);

				Element address = document.createElement("address");
				customer.appendChild(address);

				Element street_address = document.createElement("street_address");
				street_address.appendChild(document.createTextNode(street_address1));
				address.appendChild(street_address);

				Element city = document.createElement("city");
				city.appendChild(document.createTextNode(city1));
				address.appendChild(city);

				Element region = document.createElement("region");

				region.appendChild(document.createTextNode(region1));
				address.appendChild(region);

				Element postal_code = document.createElement("postal_code");
				postal_code.appendChild(document.createTextNode(postalcode1));
				address.appendChild(postal_code);

				Element country = document.createElement("country");
				country.appendChild(document.createTextNode(country1));
				address.appendChild(country);

				Element num_orders = document.createElement("num_orders");
				num_orders.appendChild(document.createTextNode(String.valueOf(num_orders1)));
				customer.appendChild(num_orders);

				Element order_value = document.createElement("order_value");
				order_value.appendChild(document.createTextNode(String.valueOf(product_value)));
				customer.appendChild(order_value);

				// System.out.println("Order number: " + resultSet.getString("ContactName"));
				// System.out.println("order date: " + resultSet.getString(""));
			}

			//gets result from second query
			ResultSet resultSet1 = null;
			resultSet1 = statement1.executeQuery(query1);
			while (resultSet1.next()) {

				String categoryname1 = (resultSet1.getString("CategoryName") == null) ? ""
						: resultSet1.getString("CategoryName");
				String productname1 = (resultSet1.getString("ProductName") == null) ? ""
						: resultSet1.getString("ProductName");
				String suppliername1 = (resultSet1.getString("CompanyName") == null) ? ""
						: resultSet1.getString("CompanyName");
				Integer units_sold1 = resultSet1.getInt("units_sold");
				Double sale_value1 = resultSet1.getDouble("value_of_product");

				Element category = document.createElement("category");
				product_list.appendChild(category);

				Element category_name = document.createElement("category_name");
				category_name.appendChild(document.createTextNode(categoryname1));
				category.appendChild(category_name);

				Element product = document.createElement("product");
				category.appendChild(product);

				Element product_name = document.createElement("product_name");
				product_name.appendChild(document.createTextNode(productname1));
				product.appendChild(product_name);

				Element supplier_name = document.createElement("supplier_name");
				supplier_name.appendChild(document.createTextNode(suppliername1));
				product.appendChild(supplier_name);

				Element units_sold = document.createElement("units_sold");
				units_sold.appendChild(document.createTextNode(String.valueOf(units_sold1)));
				product.appendChild(units_sold);

				Element sale_value = document.createElement("sale_value");
				sale_value.appendChild(document.createTextNode(String.valueOf(sale_value1)));
				product.appendChild(sale_value);

			}

			ResultSet resultSet2 = null;
			resultSet2 = statement2.executeQuery(query2);
			while (resultSet2.next()) {

				String suppliername1 = (resultSet2.getString("CompanyName") == null) ? ""
						: resultSet2.getString("CompanyName");
				String street_address1 = (resultSet2.getString("Address") == null) ? ""
						: resultSet2.getString("Address");
				String city1 = resultSet2.getString("City");

				String postalcode1 = (resultSet2.getString("PostalCode") == null) ? ""
						: resultSet2.getString("PostalCode");
				String region1 = (resultSet2.getString("Region") == null) ? "" : resultSet2.getString("Region");
//				String postalcode1 = resultSet.getString("PostalCode");
				String country1 = (resultSet2.getString("Country") == null) ? "" : resultSet2.getString("Country");
				Integer num_orders1 = resultSet2.getInt("num_orders");
				Double product_value1 = resultSet2.getDouble("product_value");

				Element supplier = document.createElement("supplier");
				supplier_list.appendChild(supplier);

				Element supplier_name = document.createElement("supplier_name");
				supplier_name.appendChild(document.createTextNode(suppliername1));
				supplier.appendChild(supplier_name);

				Element address = document.createElement("address");
				supplier.appendChild(address);

				Element street_address = document.createElement("street_address");
				street_address.appendChild(document.createTextNode(street_address1));
				address.appendChild(street_address);

				Element city = document.createElement("city");
				city.appendChild(document.createTextNode(city1));
				address.appendChild(city);

				Element region = document.createElement("region");
				region.appendChild(document.createTextNode(region1));
				address.appendChild(region);

				Element postal_code = document.createElement("postal_code");
				postal_code.appendChild(document.createTextNode(postalcode1));
				address.appendChild(postal_code);

				Element country = document.createElement("country");
				country.appendChild(document.createTextNode(country1));
				address.appendChild(country);

				Element num_products = document.createElement("num_products");
				num_products.appendChild(document.createTextNode(String.valueOf(num_orders1)));
				supplier.appendChild(num_products);

				Element product_value = document.createElement("product_value");
				product_value.appendChild(document.createTextNode(String.valueOf(product_value1)));
				supplier.appendChild(product_value);

			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();//Create a new Transformer instance 
			                                                                           
			//and a new DOMSource instance.
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource domSource = new DOMSource(document);
			StreamResult streamResult = new StreamResult(new File(path));

			// If you use
			// StreamResult result = new StreamResult(System.out);
			// the output will be pushed to the standard output ...
			// You can use that for debugging

			transformer.transform(domSource, streamResult);

			// System.out.println("Done creating XML File");

		} catch (ClassNotFoundException e) {

		} catch (SQLException e) {

		} catch (ParserConfigurationException e) {

		} catch (TransformerConfigurationException e) {

		} catch (TransformerException e) {

		} finally {
			// Always close connections, otherwise the MySQL database runs out of them.

			// Close any of the resultSet, statements, and connections that are open and
			// holding resources.
			try {
				if (resultSet != null) {
					resultSet.close();
				}

				if (statement != null) {
					statement.close();
				}

				if (connect != null) {
					connect.close();
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
}
