/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package camarseguridad;

import camaraseguridad.observable.ObservableVisitante;
import camaraseguridad.proceso.ProcesoDemonio;
import camaraseguridad.proceso.ProcesoVisitantes;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;



/**
 *
 * @author spek1
 */
public class CamaraSeguridad extends Application {

    Thread tvDer = null;
    Thread tvIzq = null;
    Thread td = null;

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        stage.setOnCloseRequest((WindowEvent e) -> {
            if (td.isAlive()) {
                td.stop();
            }
            if (tvDer.isAlive()) {
                tvDer.stop();
            }
            if (tvIzq.isAlive()) {
                tvIzq.stop();
            }
            Platform.exit();
        });
        stage.setScene(scene);
        FXMLDocumentController controladorVentana = (FXMLDocumentController) loader.getController();

        ObservableVisitante observable = new ObservableVisitante();
        observable.addObserver(controladorVentana);
        
//        observable.setContVisitantesIzq(10);
        
        ProcesoVisitantes pvDer = new ProcesoVisitantes(controladorVentana, controladorVentana.getAlColaDer(), ProcesoVisitantes.TORRETA_DER, observable);
        ProcesoVisitantes pvIzq = new ProcesoVisitantes(controladorVentana, controladorVentana.getAlColaIzq(), ProcesoVisitantes.TORRETA_IZQ, observable);
        ProcesoDemonio pd = new ProcesoDemonio(controladorVentana);

        
//        observable.addObserver(pvIzq);

        tvDer = new Thread(pvDer);
        tvDer.start();

        tvIzq = new Thread(pvIzq);
        tvIzq.start();

        td = new Thread(pd);
        td.start();

        pvDer.setEntrarEnCola(true);
        pvIzq.setEntrarEnCola(true);

        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final int PUERTO = 5000;
        byte[] buffer = new byte[1024];
        
        try{
            System.out.println("Iniciando Camara");
            DatagramSocket socketUDP = new DatagramSocket(PUERTO);
            
            DatagramPacket peticion = new DatagramPacket(buffer, buffer.length);
            socketUDP.receive(peticion);
            System.out.println("Esperando reproduccion de video\n");
            String mensaje = new String(peticion.getData());
            System.out.println(mensaje);
            
            int puertoCliente = peticion.getPort();
            InetAddress direccion = peticion.getAddress();
            
            mensaje = "Bienvenido cliente";
            buffer = mensaje.getBytes();
            DatagramPacket respuesta = new DatagramPacket(buffer, buffer.length,direccion,puertoCliente);
            System.out.println("Enviando informacion de video al cliente\n");
            socketUDP.send(respuesta);
                        
            
        } catch (SocketException ex){
            
        } catch (IOException ex) {
            Logger.getLogger(CamaraSeguridad.class.getName()).log(Level.SEVERE, null, ex);
        }  
        launch(args);
    }

}
