/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package archivador;

import java.util.Date;

/**
 *
 * @author usuario
 */
public class Archivo extends Registro {
    public int Tamano;
    public String Contenido;
    
    public Archivo(String pNombre, Folder pPadre, int pTamano, String pContenido){
        this.path = pPadre.path + pNombre + "/";
        this.nombre = pNombre;
        this.Tamano = pTamano;
        this.FechaCreacion = new Date();
        this.Contenido = pContenido;
        this.padre = pPadre;
    }
    
    
}
