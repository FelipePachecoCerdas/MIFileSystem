/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package archivador;

import com.sun.tools.javac.util.StringUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
/**
 *
 * @author usuario
 */
public class FileSystem {
    public Folder Actual;
    public RandomAccessFile discoVirtual;
    public boolean[] sectores;
    public int[] punteroSectores;
    public int tamanoSector;
    
    //Vendria siendo la función CREATE
    public FileSystem(int pCantSectores, int pTamanoSector) throws FileNotFoundException{
        //Hace algo con eso de la memoria
        File file = new File("discoVirtual.txt");
        file.delete();
        this.discoVirtual = new RandomAccessFile("discoVirtual.txt", "rw");
        
        this.sectores = new boolean[pCantSectores];
        this.punteroSectores= new int[pCantSectores];
        Arrays.fill(this.punteroSectores, -1);
        this.tamanoSector = pTamanoSector;
        Folder c = new Folder("C:", null);//Crea el folder inicial
        this.Actual = c;
    }
    
    //Vendria siendo la función FILE
    public void createFile ( String pNombre, int pTamano, String pContenido) throws IOException{
        Archivo arc = new Archivo(pNombre, Actual, pTamano, pContenido);
        String[] partesContenido=pContenido.split(String.format("(?<=\\G.{%1$d})",this.tamanoSector));
        if(partesContenido.length<=this.verificarSectores()){
            this.escribirDisco(arc, partesContenido);
            System.out.println(arc.sectorInicial);
            System.out.println(Arrays.toString(this.punteroSectores));
            System.out.println(Arrays.toString(this.sectores));
            Actual.listaCosos.add(arc);
        }else{
            System.out.println("No hay suficiente espacio");
        }
    }
    
    //Vendria siendo la función MKDIR
    public void createDir (String pNombre){
        Folder fol = new Folder(pNombre, Actual);
        Actual.listaCosos.add(fol);
    }
    
    public void modFILE(Archivo arc, String contenido) throws IOException{
        arc.Contenido = contenido;
        String[] partesContenido=contenido.split(String.format("(?<=\\G.{%1$d})",this.tamanoSector));
        if(partesContenido.length-this.misSectores(arc.sectorInicial)<=this.verificarSectores()){
            this.modificarDisco(arc, partesContenido);
            System.out.println(arc.sectorInicial);
            System.out.println(Arrays.toString(this.punteroSectores));
            System.out.println(Arrays.toString(this.sectores));
        }else{
            System.out.println("No hay suficiente espacio");
        }
    }
    
    public void ReMove( Registro pBorrar){
        if(pBorrar instanceof Archivo){
            System.out.println(((Archivo) pBorrar).sectorInicial);
            this.liberarSectores(((Archivo) pBorrar).sectorInicial);
            System.out.println("Archivo");
            System.out.println(Arrays.toString(this.punteroSectores));
            System.out.println(Arrays.toString(this.sectores));
        }
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
    
    public void verFile(Archivo arc) throws IOException{
        int sector = arc.sectorInicial;
        String contenido = "";
        while (sector != -1){
            byte[] contenidoSector = new byte[this.tamanoSector];
            System.out.println(this.tamanoSector*sector);
            this.discoVirtual.seek(sector * this.tamanoSector);
            this.discoVirtual.read(contenidoSector, 0, this.tamanoSector);
            System.out.println(this.tamanoSector*sector);
            contenido += new String(contenidoSector);
            sector = this.punteroSectores[sector];
        }
        System.out.println(contenido);
        
    }
   
    public int misSectores(int sector){
        int cant=0;
        int siguienteSector=0;
        System.out.println(sector);
        while(sector!=-1){
            siguienteSector=this.punteroSectores[sector];
            cant++;
            sector=siguienteSector;
        }
        return cant;
        
    }
    
    public int verificarSectores(){
        int disponibles=0;
        for (boolean sector: this.sectores){
            if(sector==false)
                disponibles++;
        }
        return disponibles;
    }
    
    public void escribirDisco(Archivo arc, String[] partesContenido) throws IOException{
        int sector = 0, sectorPrevio = 0;
        int cantPartes = partesContenido.length;
        int sectoresEscritos = 0;
        while(sectoresEscritos != cantPartes){
            if(this.sectores[sector] == false){
                //this.discoVirtual = new RandomAccessFile("discoVirtual.txt", "rw");
                this.discoVirtual.seek(sector*this.tamanoSector);
                int contenidoTamanno = partesContenido[sectoresEscritos].length();
                String contenidoSector = partesContenido[sectoresEscritos]+ new String(new char[this.tamanoSector-contenidoTamanno]);
                this.discoVirtual.write(contenidoSector.getBytes());
                System.out.println(contenidoSector);
                //this.discoVirtual.close();
                this.sectores[sector] = true;
                
                if(sectoresEscritos!=0){
                    this.punteroSectores[sectorPrevio] = sector;
                }
                else{
                    arc.sectorInicial=sector;
                    System.out.println(arc.sectorInicial);
                }
                sectorPrevio = sector;
                sectoresEscritos++;
            }
            sector++;
        }
        
    }
    
    public void modificarDisco(Archivo arc, String[] partesContenido) throws IOException{
        int sector = 0, sectorPrevio = 0, sectoresEscritos = 0;
        boolean ultimoOriginal=false;
        if(arc.sectorInicial != -1){
            sector = arc.sectorInicial;
            while(sectoresEscritos!=partesContenido.length){
                if(ultimoOriginal==false || (ultimoOriginal==true && this.sectores[sector] == false)){
                    this.discoVirtual.seek(sector*this.tamanoSector);
                    int contenidoTamanno = partesContenido[sectoresEscritos].length();
                    String contenidoSector = partesContenido[sectoresEscritos]+ new String(new char[this.tamanoSector-contenidoTamanno]);
                    this.discoVirtual.write(contenidoSector.getBytes());
                    System.out.println(contenidoSector);
                    //this.discoVirtual.close();
                    this.sectores[sector] = true;

                    if(sectoresEscritos!=0){
                        this.punteroSectores[sectorPrevio] = sector;
                    }
                    else{
                        arc.sectorInicial=sector;
                        System.out.println(arc.sectorInicial);
                    }
                    sectorPrevio = sector;
                    sectoresEscritos++;
                    if(ultimoOriginal==false){
                        if(this.punteroSectores[sector]!= -1)
                            sector = this.punteroSectores[sector];
                        else{
                            ultimoOriginal=true;
                            sector = -1;
                        }
                    }
                }
                if(ultimoOriginal==true)
                    sector++;
            }
            if(ultimoOriginal==false){
                this.punteroSectores[sectorPrevio]=-1;
                this.liberarSectores(sector);
            }
        }
        else{
            this.escribirDisco(arc, partesContenido);
        }
        
    }
    
    public void liberarSectores(int sector){
        int siguienteSector=0;
        System.out.println(sector);
        while(sector!=-1){
            siguienteSector=this.punteroSectores[sector];
            this.sectores[sector]=false;
            this.punteroSectores[sector]=-1;
            sector=siguienteSector;
        }
    }
}
