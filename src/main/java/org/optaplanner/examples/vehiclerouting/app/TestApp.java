package org.optaplanner.examples.vehiclerouting.app;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.examples.vehiclerouting.domain.Customer;
import org.optaplanner.examples.vehiclerouting.domain.Vehicle;
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution;
import org.optaplanner.examples.vehiclerouting.persistence.VehicleRoutingDao;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class TestApp {
    private static String xml = "<VrpVehicleRoutingSolution><id>0</id><name>bays29</name><distanceType>ROAD_DISTANCE</distanceType><distanceUnitOfMeasurement>distance</distanceUnitOfMeasurement><locationList><VrpRoadLocation id=\"1\"><id>1</id><travelDistanceMap><entry><VrpRoadLocation id=\"2\"><id>2</id><travelDistanceMap><entry><VrpRoadLocation reference=\"1\"/><double>107.0</double></entry><entry><VrpRoadLocation id=\"3\"><id>3</id><travelDistanceMap><entry><VrpRoadLocation reference=\"1\"/><double>241.0</double></entry><entry><VrpRoadLocation reference=\"2\"/><double>148.0</double></entry></travelDistanceMap></VrpRoadLocation><double>148.0</double></entry></travelDistanceMap></VrpRoadLocation><double>107.0</double></entry><entry><VrpRoadLocation reference=\"3\"/><double>241.0</double></entry></travelDistanceMap></VrpRoadLocation><VrpRoadLocation reference=\"2\"/><VrpRoadLocation reference=\"3\"/></locationList><depotList><VrpTimeWindowedDepot id=\"62\"><id>1</id><location reference=\"1\"/><readyTime>0</readyTime><dueTime>1236000</dueTime></VrpTimeWindowedDepot></depotList><vehicleList><VrpVehicle id=\"64\"><id>0</id><capacity>60</capacity><depot reference=\"62\"/></VrpVehicle><VrpVehicle id=\"65\"><id>1</id><capacity>60</capacity><depot reference=\"62\"/></VrpVehicle><VrpVehicle id=\"66\"><id>2</id><capacity>60</capacity><depot reference=\"62\"/></VrpVehicle><VrpVehicle id=\"67\"><id>3</id><capacity>60</capacity><depot reference=\"62\"/></VrpVehicle><VrpVehicle id=\"68\"><id>4</id><capacity>60</capacity><depot reference=\"62\"/></VrpVehicle></vehicleList><customerList id=\"69\"><VrpTimeWindowedCustomer id=\"70\"><id>2</id><location \t reference=\"2\"/><demand>20</demand><readyTime>830000</readyTime><dueTime>967000</dueTime><serviceDuration>90000</serviceDuration></VrpTimeWindowedCustomer><VrpTimeWindowedCustomer id=\"71\"><id>3</id><location class=\"VrpRoadLocation\" reference=\"3\"/><demand>10</demand><readyTime>625000</readyTime><dueTime>670000</dueTime><serviceDuration>90000</serviceDuration></VrpTimeWindowedCustomer></customerList></VrpVehicleRoutingSolution>";

    public VehicleRoutingSolution readFromString(VehicleRoutingDao solutionDao, String xml) {
        return solutionDao.readSolutionFromString(xml);
    }

    private VehicleRoutingSolution readFromFile(VehicleRoutingDao solutionDao, File xml) {
        return solutionDao.readSolutionFromFile(xml);
    }


    public String solve(String str) {
        SolverFactory<VehicleRoutingSolution> solverFactory = SolverFactory.createFromXmlResource("org/optaplanner/examples/vehiclerouting/solver/vehicleRoutingSolverConfig.xml");
        Solver<VehicleRoutingSolution> solver = solverFactory.buildSolver();

        VehicleRoutingDao solutionDao = new VehicleRoutingDao();
        VehicleRoutingSolution solution = readFromString(solutionDao, str);

        solver.solve(solution);

        VehicleRoutingSolution bestSolution = solver.getBestSolution();
        return getXml(bestSolution.getVehicleList());
    }

    private void getCustomers(List<Customer> customers, Customer customer) {
        if (customer != null) {
            if (customer.getNextCustomer() == null)
                customers.add(customer);
            else {
                customers.add(customer);
                getCustomers(customers, customer.getNextCustomer());
            }
        }
    }

    public static void main(String[] args) {
        TestApp app = new TestApp();
        app.solve(xml);
    }

    private String getXml(List<Vehicle> vehicleList) {
        Document dom;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.newDocument();

            Element rootEle = dom.createElement("routing");

            for (Vehicle vehicle : vehicleList) {
                Element veh = dom.createElement("vehicle");
                veh.setAttribute("name", vehicle.toString());

                List<Customer> customers = new ArrayList<>();
                getCustomers(customers, vehicle.getNextCustomer());
                int sequence = 0;

                for (Customer customer : customers) {
                    Element cust = dom.createElement("customer"),
                            id = dom.createElement("id"),
                            seq = dom.createElement("sequence");

                    id.appendChild(dom.createTextNode(customer.toString()));
                    seq.appendChild(dom.createTextNode(String.valueOf(++sequence)));

                    cust.appendChild(id);
                    cust.appendChild(seq);

                    veh.appendChild(cust);
                }

                rootEle.appendChild(veh);
            }

            dom.appendChild(rootEle);

            try {
                StringWriter writer = new StringWriter(0);
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

                tr.transform(new DOMSource(dom), new StreamResult(writer));

                return writer.toString();

            } catch (TransformerException te) {
                System.out.println(te.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }

        return "";
    }
}
