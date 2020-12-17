/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package archivador;

/**
 *
 * @author usuario
 */
public class FileSystem {
    public Folder Actual;
    
    //Vendria siendo la función CREATE
    public FileSystem(int pCantSectores, int pTamanoSector){
        //Hace algo con eso de la memoria
        
        
        Folder c = new Folder("C:", null);//Crea el folder inicial
        this.Actual = c;
    }
    
    //Vendria siendo la función FILE
    public void createFile ( String pNombre, int pTamano, String pContenido){
        Archivo arc = new Archivo(pNombre, Actual, 0, pContenido);
        Actual.listaCosos.add(arc);
    }
    
    //Vendria siendo la función MKDIR
    public void createDir (String pNombre){
        Folder fol = new Folder(pNombre, Actual);
        Actual.listaCosos.add(fol);
    }
    
    public void modFILE(Archivo pFIle, String pNewCont){
        pFIle.Contenido = pNewCont;
    }
    
    public void ReMove( Registro pBorrar){
        Actual.listaCosos.remove(pBorrar);
    }
    
    public void goUp(){
        if (Actual.padre != null){
            Actual = Actual.padre;
        }
    }
    
    public void goDown(Folder pNext){
        Actual = pNext;
    }
}
