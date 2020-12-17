/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package archivador;

import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author usuario
 */
public class Folder extends Registro {
    public ArrayList<Registro> listaCosos = new ArrayList<Registro>();
    
    public Folder(String pNombre, Folder pPadre){
        if (pPadre != null) {
            this.path = pPadre.path + pNombre + "/";
            this.nombre = pNombre;
            this.FechaCreacion = new Date();
            this.padre = pPadre;
        }
        else{
            this.path = pNombre + "/";
            this.nombre = pNombre;
            this.FechaCreacion = new Date();
        }
    }
    @Override
    public void cambiarPath(){
        String nuevoPath = "";
        Folder p = padre;
        while(p != null){
            nuevoPath = p.nombre + "/" + nuevoPath;
            p = p.padre;
        }
        nuevoPath = nuevoPath + this.nombre + "/";
        this.path = nuevoPath;
        //Ahora se debe de cambiar el path de los hijos
        for(Registro r : this.listaCosos){
            r.cambiarPath();
        }
    }
    
}
