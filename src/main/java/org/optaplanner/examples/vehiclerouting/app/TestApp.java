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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestApp {


    public void solveFromFile(File vrpXmlInputFile) {
        SolverFactory<VehicleRoutingSolution> solverFactory = SolverFactory.createFromXmlResource("org/optaplanner/examples/vehiclerouting/solver/vehicleRoutingSolverConfig.xml");
        Solver<VehicleRoutingSolution> solver = solverFactory.buildSolver();

        VehicleRoutingDao solutionDao = new VehicleRoutingDao();
        VehicleRoutingSolution solution = solutionDao.readSolution(vrpXmlInputFile);

        solver.solve(solution);

        VehicleRoutingSolution bestSolution = solver.getBestSolution();
        System.out.println("Best solution: " + (bestSolution));
        writeXml(bestSolution.getVehicleList(), "out.xml");

//        for (Vehicle vehicle : bestSolution.getVehicleList()) {
//            System.out.println(vehicle);
//
//            if (vehicle.getNextCustomer() != null) {
//                getCustomers(vehicle.getNextCustomer());
//            } else {
//                System.out.println("No route");
//            }
//        }
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
        app.solveFromFile(new File("D:\\test.xml"));
    }

    private void writeXml(List<Vehicle> vehicleList, String name) {
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
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                tr.transform(new DOMSource(dom),
                        new StreamResult(new FileOutputStream(name)));

            } catch (TransformerException te) {
                System.out.println(te.getMessage());
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
    }
}
