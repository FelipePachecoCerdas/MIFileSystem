/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import archivador.Archivo;
import archivador.FileSystem;
import archivador.Folder;
import archivador.Registro;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author usuario
 */
public class Gui extends javax.swing.JFrame {

  FileSystem coso;
  Registro selected, seleccionado = null;
  boolean NadaSelected = true;
  boolean copiando = false, moviendo = false;
  ArrayList<Registro> encontrados;
  ArrayList<Registro> arbolados = new ArrayList<>();
  Gui gui;

  /**
   * Creates new form Gui
   */
  public Gui(FileSystem este) {

    this.coso = este;
    initComponents();
    this.clipboard.setText("");
    actualizarCosos();
    jButton5.setEnabled(false);
    BCopyVV.setEnabled(false);
    BMover.setEnabled(false);

    this.setLocationRelativeTo(null);
    this.getContentPane().setBackground(Color.decode("#121212"));

    lista.setCellRenderer(new ListCellRenderer());
    log.setCellRenderer(new TreeCellRenderer());

    MouseListener ml = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        for (int i = 0; i < log.getRowCount(); i++) {
          log.expandRow(i);
        }

        int selRow = log.getRowForLocation(e.getX(), e.getY());
        TreePath selPath = log.getPathForLocation(e.getX(), e.getY());
        if (selRow != -1) {
          if (e.getClickCount() == 2) {
            Registro p = arbolados.get(selRow);

            if (p instanceof Folder) {
              coso.Actual = (Folder) p;
            } else {
              coso.Actual = p.padre;
            }
            gui.actualizarCosos();

            //myDoubleClick(selRow, selPath);
          }
        }
      }
    };
    log.addMouseListener(ml);

    this.lista.addMouseListener(new MouseAdapter() {

      public void mouseClicked(MouseEvent evt) {
        JList list = (JList) evt.getSource();
        if (evt.getClickCount() == 2) {

          // Double-click detected
          int index = list.locationToIndex(evt.getPoint());

          if (coso.Actual.listaCosos.get(index) instanceof Folder) {
            coso.Actual = (Folder) coso.Actual.listaCosos.get(index);
            actualizarCosos();
          } else {
            Archivo leer = (Archivo) coso.Actual.listaCosos.get(index);
            String contenido = "";
            try {
              contenido = coso.verFile(leer);
            } catch (IOException ex) {
              Logger.getLogger(Gui.class.getName()).log(Level.SEVERE, null, ex);
            }

            JTextArea textAreaXD = new JTextArea(contenido);
            textAreaXD.setColumns(30);
            textAreaXD.setRows(10);
            textAreaXD.setEditable(false);
            textAreaXD.setLineWrap(true);
            textAreaXD.setWrapStyleWord(true);
            textAreaXD.setSize(textAreaXD.getPreferredSize().width, textAreaXD.getPreferredSize().height);

            String[] options = new String[]{"Modificar", "Cerrar"};
            int response = JOptionPane.showOptionDialog(null, textAreaXD, leer.nombre + "." + leer.extension + " (Lectura)",
              JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
              null, options, options[0]);

            if (response == 0) {
              // MODIFICAR

              JTextArea textArea = new JTextArea(contenido);
              textArea.setColumns(30);
              textArea.setRows(10);
              textArea.setLineWrap(true);
              textArea.setWrapStyleWord(true);
              textArea.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);

              String[] optionsXD = new String[]{"Guardar", "Cancelar"};
              int ret = JOptionPane.showOptionDialog(null, new JScrollPane(textArea), leer.nombre + "." + leer.extension + " (Modificación)",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, optionsXD, optionsXD[0]);

              String nuevoContenido = "";
              if (ret == 0) {
                nuevoContenido = textArea.getText();
                try {
                  boolean ok = coso.modFILE(leer, nuevoContenido);
                  if (!ok) {
                    JOptionPane.showMessageDialog(null, "No hay espacio suficiente en el disco para crear el archivo", "Memoria Llena", JOptionPane.INFORMATION_MESSAGE);

                    return;
                  }
                  leer.FechaModificacion = new Date();
                } catch (IOException ex) {
                  Logger.getLogger(Gui.class.getName()).log(Level.SEVERE, null, ex);
                }
              }

            } else {
              // Cerrar
              return;
            }

          }
        }

      }
    }
    );

    gui = this;
    selected = coso.Actual;
    this.lista.addListSelectionListener(new ListSelectionListener() {

      public void valueChanged(ListSelectionEvent arg0) {

        if (!arg0.getValueIsAdjusting()) {
          int a = lista.getSelectedIndex();
          if (a != -1 && a <= coso.Actual.listaCosos.size()) {
            selected = coso.Actual.listaCosos.get(a);

            jButton5.setEnabled(true);
            BCopyVV.setEnabled(true);
            BMover.setEnabled(true);
            //ButtonMover.setText("Quitar selección");
          } else {
            selected = coso.Actual;

            jButton5.setEnabled(false);
            if (!copiando) {
              BCopyVV.setEnabled(false);
            }
            if (!moviendo) {
              BMover.setEnabled(false);
            }

          }

        }
        //gui.actualizarCosos();
      }
    });

  }

  public void actualizarCosos() {
    LabelPathW.setText(this.coso.Actual.path);
    DefaultListModel modelo = new DefaultListModel();
    for (int i = 0; i < this.coso.Actual.listaCosos.size(); i++) {
      String tipo = (this.coso.Actual.listaCosos.get(i) instanceof Folder) ? "D" : "A";

      Registro r = this.coso.Actual.listaCosos.get(i);
      String strRegistro = tipo + r.nombre + ((r instanceof Archivo) ? ("." + ((Archivo) r).extension) : "");
      modelo.addElement(strRegistro);
    }
    lista.setModel(modelo);
    selected = coso.Actual;

    if (moviendo) {
      if (seleccionado.padre == coso.Actual) {
        this.BMover.setText("Renombrar");
      } else {
        this.BMover.setText("Mover Aquí");
      }
    }

    arbolados = new ArrayList<>();
    DefaultMutableTreeNode root = popularArbol(coso.root);

    DefaultTreeModel tm = new DefaultTreeModel(root);// = (root);//(root);//= new TreeModel();
    log.setModel(tm);
    for (int i = 0; i < log.getRowCount(); i++) {
      log.expandRow(i);
    }

  }

  public DefaultMutableTreeNode popularArbol(Registro r) {
    DefaultMutableTreeNode yo;

    if (r instanceof Folder) {
      Folder f = (Folder) r;
      yo = new DefaultMutableTreeNode("D" + f.nombre + ((f == coso.Actual) ? "  [←]" : ""));
      arbolados.add(r);

      for (Registro elem : f.listaCosos) {
        yo.add(popularArbol(elem));
      }

    } else {
      Archivo a = (Archivo) r;
      yo = new DefaultMutableTreeNode("A" + a.nombre + "." + a.extension);
      arbolados.add(r);
    }
    return yo;
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    ButtonAnalize1 = new javax.swing.JButton();
    LabelPathW = new javax.swing.JLabel();
    jButton1 = new javax.swing.JButton();
    jScrollPane1 = new javax.swing.JScrollPane();
    lista = new javax.swing.JList<>();
    jButton2 = new javax.swing.JButton();
    jButton3 = new javax.swing.JButton();
    jButton4 = new javax.swing.JButton();
    jButton5 = new javax.swing.JButton();
    BMover = new javax.swing.JButton();
    BCopyVV = new javax.swing.JButton();
    limpiar = new javax.swing.JButton();
    BCopyVR = new javax.swing.JButton();
    BModificar = new javax.swing.JButton();
    jButton6 = new javax.swing.JButton();
    jLabel2 = new javax.swing.JLabel();
    jLabel3 = new javax.swing.JLabel();
    LabelPath = new javax.swing.JLabel();
    clipboard = new javax.swing.JLabel();
    jButton9 = new javax.swing.JButton();
    jScrollPane2 = new javax.swing.JScrollPane();
    log = new javax.swing.JTree();

    ButtonAnalize1.setBackground(Color.decode("#4ecca3"));
    ButtonAnalize1.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
    ButtonAnalize1.setText("Analizar");
    ButtonAnalize1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        ButtonAnalize1ActionPerformed(evt);
      }
    });

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    LabelPathW.setBackground(new java.awt.Color(255, 255, 255));
    LabelPathW.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
    LabelPathW.setForeground(new java.awt.Color(255, 255, 255));
    LabelPathW.setText("Portapapeles:");
    getContentPane().add(LabelPathW, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 110, 540, 30));

    jButton1.setBackground(Color.decode("#4ecca3"));
    jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/return.png"))); // NOI18N
    jButton1.setToolTipText("");
    jButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton1ActionPerformed(evt);
      }
    });
    getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 110, 50, 30));

    lista.setBackground(Color.decode("#433d3c"));
    lista.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
    lista.setForeground(new java.awt.Color(255, 255, 255));
    jScrollPane1.setViewportView(lista);

    getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 160, 700, 230));

    jButton2.setBackground(Color.decode("#4ecca3"));
    jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/next.png"))); // NOI18N
    jButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton2ActionPerformed(evt);
      }
    });
    getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 480, 30, 30));

    jButton3.setBackground(Color.decode("#c4fb6d"));
    jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/newDir.png"))); // NOI18N
    jButton3.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton3ActionPerformed(evt);
      }
    });
    getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 410, 50, 40));

    jButton4.setBackground(Color.decode("#c4fb6d"));
    jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/newFile.png"))); // NOI18N
    jButton4.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton4ActionPerformed(evt);
      }
    });
    getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 410, 50, 40));

    jButton5.setBackground(Color.decode("#d54062"));
    jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/borrador.png"))); // NOI18N
    jButton5.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton5ActionPerformed(evt);
      }
    });
    getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 410, 50, 40));

    BMover.setBackground(Color.decode("#4ecca3"));
    BMover.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    BMover.setForeground(new java.awt.Color(0, 0, 0));
    BMover.setText("Mover");
    BMover.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        BMoverActionPerformed(evt);
      }
    });
    getContentPane().add(BMover, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 470, 110, 40));

    BCopyVV.setBackground(Color.decode("#4ecca3"));
    BCopyVV.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    BCopyVV.setForeground(new java.awt.Color(0, 0, 0));
    BCopyVV.setText("Copiar");
    BCopyVV.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        BCopyVVActionPerformed(evt);
      }
    });
    getContentPane().add(BCopyVV, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 410, 110, 40));

    limpiar.setBackground(Color.decode("#4ecca3"));
    limpiar.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    limpiar.setForeground(new java.awt.Color(0, 0, 0));
    limpiar.setText("Limpiar");
    limpiar.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        limpiarActionPerformed(evt);
      }
    });
    getContentPane().add(limpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 560, 80, -1));

    BCopyVR.setBackground(Color.decode("#4ecca3"));
    BCopyVR.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    BCopyVR.setForeground(new java.awt.Color(0, 0, 0));
    BCopyVR.setText("Exportar");
    BCopyVR.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        BCopyVRActionPerformed(evt);
      }
    });
    getContentPane().add(BCopyVR, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 470, 110, 40));

    BModificar.setBackground(Color.decode("#4ecca3"));
    BModificar.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    BModificar.setForeground(new java.awt.Color(0, 0, 0));
    BModificar.setText("Propiedades");
    BModificar.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        BModificarActionPerformed(evt);
      }
    });
    getContentPane().add(BModificar, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 410, 110, 40));

    jButton6.setBackground(Color.decode("#4ecca3"));
    jButton6.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    jButton6.setForeground(new java.awt.Color(0, 0, 0));
    jButton6.setText("Buscar");
    jButton6.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton6ActionPerformed(evt);
      }
    });
    getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 470, 110, 40));

    jLabel2.setBackground(Color.decode("#4ecca3"));
    jLabel2.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    jLabel2.setForeground(Color.decode("#29c7ac"));
    getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 560, 50, 50));

    jLabel3.setBackground(new java.awt.Color(255, 255, 255));
    jLabel3.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
    jLabel3.setForeground(Color.decode("#29c7ac"));
    jLabel3.setText("MI File System");
    getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 20, -1, -1));

    LabelPath.setBackground(new java.awt.Color(255, 255, 255));
    LabelPath.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
    LabelPath.setForeground(new java.awt.Color(255, 255, 255));
    LabelPath.setText("Portapapeles:");
    getContentPane().add(LabelPath, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 530, 110, 30));

    clipboard.setBackground(new java.awt.Color(255, 255, 255));
    clipboard.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
    clipboard.setForeground(new java.awt.Color(255, 255, 255));
    clipboard.setText("path");
    getContentPane().add(clipboard, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 530, 570, 30));

    jButton9.setBackground(Color.decode("#4ecca3"));
    jButton9.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    jButton9.setForeground(new java.awt.Color(0, 0, 0));
    jButton9.setText("Importar");
    jButton9.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton9ActionPerformed(evt);
      }
    });
    getContentPane().add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 410, 110, 40));

    log.setBackground(Color.decode("#433d3c"));
    log.setForeground(new java.awt.Color(255, 255, 255));
    jScrollPane2.setViewportView(log);

    getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 110, 210, 480));

    pack();
  }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
      // TODO add your handling code here:
      if (coso.Actual.padre != null) {
        coso.Actual = coso.Actual.padre;
        actualizarCosos();
      }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
      // TODO add your handling code here:
      lista.clearSelection();
    }//GEN-LAST:event_jButton2ActionPerformed

  public void ventanaXD() {
    JFrame jf = new JFrame("xd");
    jf.setLocationRelativeTo(null);
    jf.setLayout(null);

  }

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
      // TODO add your handling code here:
      String name = JOptionPane.showInputDialog(this, "Indique el nombre del directorio", "Nombre del Directorio", JOptionPane.QUESTION_MESSAGE);
      if (name == null || name.equals("")) {
        return;
      }

      Registro encontrado = null;
      for (int i = 0; i < coso.Actual.listaCosos.size(); i++) {
        Registro r = coso.Actual.listaCosos.get(i);
        if (r instanceof Folder && r.nombre.equals(name)) {
          encontrado = r;
        }
      }

      if (encontrado != null) {

        String[] options = new String[]{"Sobreescribir", "Cancelar"};
        int response = JOptionPane.showOptionDialog(null, "Ya existe un directorio con ese nombre. ¿Qué desea hacer?", "Directorio ya existe",
          JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
          null, options, options[0]);

        if (response == 0) {
          coso.ReMove(encontrado);
        } else {
          // Cancelar
          return;
        }
      }
      // BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

      //String name = reader.readLine();
      coso.createDir(name);
      actualizarCosos();

    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
      // TODO add your handling code here:
      int a = lista.getSelectedIndex();
      if (a != -1 && a <= coso.Actual.listaCosos.size()) {
        Registro elim = coso.Actual.listaCosos.get(a);
        coso.ReMove(elim);
        actualizarCosos();
      }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
      // TODO add your handling code here:
      String name = JOptionPane.showInputDialog(this, "Indique el nombre del archivo", "Nombre del Archivo", JOptionPane.QUESTION_MESSAGE);
      if (name == null || name.equals("")) {
        return;
      }

      String ext = JOptionPane.showInputDialog(this, "Indique la extensión del archivo (pdf, doc, txt, ...)", "Extensión del Archivo", JOptionPane.QUESTION_MESSAGE);
      if (ext == null || ext.equals("")) {
        return;
      }

      /*
      String contenido = JOptionPane.showInputDialog(this, "Indique el contenido del archivo", "Contenido del Archivo", JOptionPane.QUESTION_MESSAGE);
      if (contenido == null) {
        contenido = "";
      }*/
      JTextArea textArea = new JTextArea("");
      textArea.setColumns(30);
      textArea.setRows(10);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);
      textArea.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);

      String[] optionsXD = new String[]{"Guardar", "Cancelar"};
      int ret = JOptionPane.showOptionDialog(null, new JScrollPane(textArea), name + "." + ext + " (Creación)",
        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
        null, optionsXD, optionsXD[0]);

      String contenido = "";
      if (ret == 0) {
        contenido = textArea.getText();
      } else {
        return;
      }

      Registro encontrado = null;
      for (int i = 0; i < coso.Actual.listaCosos.size(); i++) {
        Registro r = coso.Actual.listaCosos.get(i);
        if (r instanceof Archivo && r.nombre.equals(name) && ((Archivo) r).extension.equals(ext)) {
          encontrado = r;
        }
      }

      if (encontrado != null) {

        String[] options = new String[]{"Sobreescribir", "Cancelar"};
        int response = JOptionPane.showOptionDialog(null, "Ya existe un archivo con ese nombre y esa extensión. ¿Qué desea hacer?", "Archivo ya existe",
          JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
          null, options, options[0]);

        if (response == 0) {
          coso.ReMove(encontrado);
        } else {
          // Cancelar
          return;
        }
      }

      int tamannoArchivo = contenido.length();
      try {
        boolean ok = coso.createFile(name, tamannoArchivo, contenido, ext);

        if (!ok) {
          JOptionPane.showMessageDialog(null, "No hay espacio suficiente en el disco para crear el archivo", "Memoria Llena", JOptionPane.INFORMATION_MESSAGE);

        }

      } catch (IOException ex) {
        Logger.getLogger(Gui.class
          .getName()).log(Level.SEVERE, null, ex);
      }
      actualizarCosos();

    }//GEN-LAST:event_jButton4ActionPerformed

    private void BMoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BMoverActionPerformed
      // TODO add your handling code here:
      if (moviendo) {

        if (seleccionado.padre == coso.Actual) {
          // RENAME
          String cosa = (seleccionado instanceof Folder) ? "Directorio" : "Archivo";
          String nuevoNombre = JOptionPane.showInputDialog(this, "Indique el nuevo nombre del " + cosa, "Nombre del " + cosa, JOptionPane.QUESTION_MESSAGE);
          if (nuevoNombre == null || nuevoNombre.equals("") || nuevoNombre.equals(seleccionado.nombre)) {
            moviendo = false;
            this.BMover.setText("Mover");
            this.BMover.setEnabled(false);
            this.clipboard.setText("");
            actualizarCosos();
            return;
          }
          seleccionado.nombre = nuevoNombre;

          seleccionado.cambiarPath();
          seleccionado.FechaModificacion = new Date();

          moviendo = false;
          this.BMover.setText("Mover");
          this.BMover.setEnabled(false);
          this.clipboard.setText("");
          actualizarCosos();

          return;
        }

        if (seleccionado instanceof Folder && coso.contiene((Folder) seleccionado, coso.Actual)) {
          JOptionPane.showMessageDialog(null, "La carpeta destino es subcarpeta de la carpeta a mover, por lo que no se puede realizar la operación", "Operación Imposible", JOptionPane.INFORMATION_MESSAGE);

          return;
        }

        Folder NuevoDir = coso.Actual;
        //Lo agrega al nuevo folder
        NuevoDir.listaCosos.add(seleccionado);
        //Se elimina del folder anterior
        seleccionado.padre.listaCosos.remove(seleccionado);
        //Se le vincula el nuevo padre
        seleccionado.padre = NuevoDir;

        seleccionado.cambiarPath();
        seleccionado.FechaModificacion = new Date();

        moviendo = false;
        this.BMover.setText("Mover");
        this.BMover.setEnabled(false);
        this.clipboard.setText("");
      } else {
        moviendo = true;
        seleccionado = selected;
        this.clipboard.setText(selected.path);
        this.BMover.setText("Mover Aquí");
        this.limpiar.setEnabled(true);

        copiando = false;
        this.BCopyVV.setText("Copiar");
      }
      actualizarCosos();
    }//GEN-LAST:event_BMoverActionPerformed

    private void BCopyVVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BCopyVVActionPerformed
      // TODO add your handling code here:
      if (copiando) {

        if (seleccionado instanceof Folder && coso.contiene((Folder) seleccionado, coso.Actual)) {
          JOptionPane.showMessageDialog(null, "La carpeta destino es subcarpeta de la carpeta a copiar, por lo que no se puede realizar la operación", "Operación Imposible", JOptionPane.INFORMATION_MESSAGE);

          return;
        }

        Registro encontrado = null;
        for (int i = 0; i < coso.Actual.listaCosos.size(); i++) {
          if (coso.Actual.listaCosos.get(i).nombre.equals(seleccionado.nombre)) {
            if (coso.Actual.listaCosos.get(i) instanceof Archivo && seleccionado instanceof Archivo) {
              Archivo a = (Archivo) coso.Actual.listaCosos.get(i);
              if (a.extension.equals(((Archivo) seleccionado).extension)) {
                encontrado = coso.Actual.listaCosos.get(i);

              }
            } else {
              encontrado = coso.Actual.listaCosos.get(i);
            }
          }
        }

        if (encontrado != null) {
          // RENAME seleccionado.padre == coso.Actual
          String cosa = (seleccionado instanceof Folder) ? "Directorio" : "Archivo";
          String[] options = new String[]{"Renombrar copia", "Cancelar"};
          int response = JOptionPane.showOptionDialog(null, "Ya existe un " + cosa + " con ese nombre. ¿Qué desea hacer?", cosa + " ya existe",
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
            null, options, options[0]);

          if (response == 0) {

            // VALIDARRRR
            if (seleccionado.getSectores(coso) > coso.verificarSectores()) {
              JOptionPane.showMessageDialog(null, "No hay espacio suficiente en el disco para realizar el copiado", "Memoria Llena", JOptionPane.INFORMATION_MESSAGE);
              return;
            }

            String nuevoNombre = JOptionPane.showInputDialog(this, "Indique el nombre del " + cosa, "Nombre del " + cosa, JOptionPane.QUESTION_MESSAGE);
            if (nuevoNombre == null || nuevoNombre.equals("") || nuevoNombre.equals(seleccionado.nombre)) {
              return;
            }

            Folder destino = coso.Actual;

            destino.agregarConNombre(seleccionado, coso, nuevoNombre);

          } else {
            // Cancelar
            return;
          }

          actualizarCosos();

          return;
        }

        Folder destino = coso.Actual;

        // VALIDARRRR
        if (seleccionado.getSectores(coso) > coso.verificarSectores()) {
          JOptionPane.showMessageDialog(null, "No hay espacio suficiente en el disco para realizar el copiado", "Memoria Llena", JOptionPane.INFORMATION_MESSAGE);
          return;
        }
        destino.agregar(seleccionado, coso);

      } else {
        copiando = true;
        seleccionado = selected;
        this.clipboard.setText(selected.path);
        this.BCopyVV.setText("Pegar");
        this.limpiar.setEnabled(true);

        moviendo = false;
        this.BMover.setText("Mover");
      }

      actualizarCosos();
    }//GEN-LAST:event_BCopyVVActionPerformed

    private void BModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BModificarActionPerformed
      // PROP
      Registro r = selected;
      String props;

      if (r instanceof Archivo) {
        Archivo a = (Archivo) r;
        Format formatter = new SimpleDateFormat("dd/MM/yyyy KK:mm:ss a");

        props = "Tipo: Archivo" + "\n"
          + "Nombre: " + a.nombre + "\n"
          + "Extensión: " + a.extension + "\n"
          + "Ruta: " + a.path + "\n"
          + "Tamaño: " + a.getSize() + " bytes\n"
          + "Fecha de Creación: " + formatter.format(a.FechaCreacion) + "\n"
          + "Fecha de Modificación: " + formatter.format(a.FechaModificacion) + "\n";

        String[] optionsXD = new String[]{"Cerrar"};
        int ret = JOptionPane.showOptionDialog(null, props, a.nombre + "." + a.extension + " (Propiedades)",
          JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
          null, optionsXD, optionsXD[0]);
      } else {

        Folder f = (Folder) r;
        Format formatter = new SimpleDateFormat("dd/MM/yyyy KK:mm:ss a");

        props = "Tipo: Directorio" + "\n"
          + "Nombre: " + f.nombre + "\n"
          + "Ruta: " + f.path + "\n"
          + "Tamaño: " + f.getSize() + " bytes\n"
          + "Contenido: " + f.listaCosos.size() + " elementos\n"
          + "Fecha de Creación: " + formatter.format(f.FechaCreacion) + "\n"
          + "Fecha de Modificación: " + formatter.format(f.FechaModificacion) + "\n";

        if (f.padre == null) {
          try {
            props += getSectores();

          } catch (IOException ex) {
            Logger.getLogger(Gui.class
              .getName()).log(Level.SEVERE, null, ex);
          }
        }

        String[] optionsXD = new String[]{"Cerrar"};
        int ret = JOptionPane.showOptionDialog(null, props, f.nombre + " (Propiedades)",
          JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
          null, optionsXD, optionsXD[0]);

      }
    }//GEN-LAST:event_BModificarActionPerformed

  public String getSectores() throws IOException {
    String sect = "";

    for (int k = 0; k < coso.sectores.length; k++) {
      sect += "[" + k + "] ";
      byte[] contenidoSector = new byte[coso.tamanoSector];
      //System.out.println(this.tamanoSector*sector);
      coso.discoVirtual.seek(k * coso.tamanoSector);
      coso.discoVirtual.read(contenidoSector, 0, coso.tamanoSector);
      //System.out.println(this.tamanoSector*sector);

      //contenido += new String(contenidoSector);
      for (int i = 0; i < contenidoSector.length; i++) {
        if (contenidoSector[i] != 0) {
          if ((char) contenidoSector[i] == '\n') {
            sect += "\\n";
          } else {
            sect += (char) contenidoSector[i];
          }
        }
      }

      sect += "\n";

    }

    return sect;
  }
    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
      // TODO add your handling code here:

      String name = JOptionPane.showInputDialog(this, "Hilera de búsqueda:", "Búsqueda", JOptionPane.QUESTION_MESSAGE);
      if (name == null || name.equals("")) {
        return;
      }

      encontrados = coso.buscar(name);

      JList superLista = new JList();
      superLista.setCellRenderer(new ListCellRenderer());
      superLista.setSize(superLista.getPreferredSize().width, superLista.getPreferredSize().height);

      DefaultListModel modelo = new DefaultListModel();
      for (Registro r : encontrados) {
        String tipo = (r instanceof Folder) ? "D" : "A";

        String strRegistro = tipo + r.nombre + ((r instanceof Archivo) ? ("." + ((Archivo) r).extension) : "") + " [ " + r.path + " ]";
        modelo.addElement(strRegistro);
      }
      superLista.setModel(modelo);

      superLista.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent evt) {
          JList list = (JList) evt.getSource();
          if (evt.getClickCount() == 2) {

            // Double-click detected
            int index = superLista.locationToIndex(evt.getPoint());

            Registro p = encontrados.get(index);
            if (p instanceof Folder) {
              coso.Actual = (Folder) p;
            } else {
              coso.Actual = p.padre;
            }
            gui.actualizarCosos();
          }

        }
      }
      );

      String[] options = new String[]{"Cerrar"};
      int response = JOptionPane.showOptionDialog(this, superLista, "Resultados de Búsqueda",
        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
        null, options, options[0]);

    }//GEN-LAST:event_jButton6ActionPerformed

  private void ButtonAnalize1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonAnalize1ActionPerformed

  }//GEN-LAST:event_ButtonAnalize1ActionPerformed

  private void limpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limpiarActionPerformed

    if (moviendo) {
      moviendo = false;
      this.BMover.setText("Mover");
      this.BMover.setEnabled(false);
      this.clipboard.setText("");
      this.limpiar.setEnabled(false);
    }
    if (copiando) {
      copiando = false;
      this.BCopyVV.setText("Copiar");
      this.BCopyVV.setEnabled(false);
      this.clipboard.setText("");
      this.limpiar.setEnabled(false);
    }
    actualizarCosos();

  }//GEN-LAST:event_limpiarActionPerformed

  private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
    // IMPPORTAR
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    int returnVal = chooser.showOpenDialog(null);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File f = chooser.getSelectedFile();
      String nf = "";
      if (f.getName().lastIndexOf(".") == -1) {
        nf = f.getName();
      } else {
        nf = f.getName().substring(0, f.getName().lastIndexOf("."));
      }

      try {

        if (coso.getSectoresImportar(f) > coso.verificarSectores()) {
          JOptionPane.showMessageDialog(null, "No hay espacio suficiente en el disco para realizar el copiado", "Memoria Llena", JOptionPane.INFORMATION_MESSAGE);
          return;
        }

        Registro encontrado = null;
        for (int i = 0; i < coso.Actual.listaCosos.size(); i++) {
          Registro r = coso.Actual.listaCosos.get(i);
          if (nf.equals(r.nombre)) {
            if (f.isFile() && r instanceof Archivo) {
              String ef = f.getName().substring(f.getName().lastIndexOf(".") + 1, f.getName().length());
              if (ef.equals(((Archivo) r).extension)) {
                encontrado = coso.Actual.listaCosos.get(i);

              }
            } else {
              encontrado = coso.Actual.listaCosos.get(i);
            }
          }
        }

        if (encontrado != null) {

          String cosa = (f.isDirectory()) ? "Directorio" : "Archivo";
          String[] options = new String[]{"Renombrar copia", "Cancelar"};
          int response = JOptionPane.showOptionDialog(null, "Ya existe un " + cosa + " con ese nombre. ¿Qué desea hacer?", cosa + " ya existe",
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
            null, options, options[0]);

          if (response == 0) {

            String nuevoNombre = JOptionPane.showInputDialog(this, "Indique el nombre del " + cosa, "Nombre del " + cosa, JOptionPane.QUESTION_MESSAGE);
            if (nuevoNombre == null || nuevoNombre.equals("") || nuevoNombre.equals(nf)) {
              return;
            }

            coso.importar(f, nuevoNombre);

          } else {
            // Cancelar
            return;
          }

          actualizarCosos();

          return;

        } else {
          coso.importar(f, "");

        }
      } catch (IOException ex) {
        Logger.getLogger(Gui.class
          .getName()).log(Level.SEVERE, null, ex);
      }
    }
    this.actualizarCosos();
  }//GEN-LAST:event_jButton9ActionPerformed

  private void BCopyVRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BCopyVRActionPerformed
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int returnVal = chooser.showOpenDialog(null);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File f = chooser.getSelectedFile();
      try {
        if (coso.exportar(f, selected)) {

        } else {
          //
          JOptionPane.showMessageDialog(null, "No se ha podido exportar el archivo o directorio porque ya existe uno con ese nombre en la máquina", "Operación Imposible", JOptionPane.INFORMATION_MESSAGE);

        }

      } catch (IOException ex) {
        Logger.getLogger(Gui.class
          .getName()).log(Level.SEVERE, null, ex);
      }
    }

    this.actualizarCosos();


  }//GEN-LAST:event_BCopyVRActionPerformed

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) throws FileNotFoundException {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
    /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
     */
    try {
      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;

        }
      }
    } catch (ClassNotFoundException ex) {
      java.util.logging.Logger.getLogger(Gui.class
        .getName()).log(java.util.logging.Level.SEVERE, null, ex);

    } catch (InstantiationException ex) {
      java.util.logging.Logger.getLogger(Gui.class
        .getName()).log(java.util.logging.Level.SEVERE, null, ex);

    } catch (IllegalAccessException ex) {
      java.util.logging.Logger.getLogger(Gui.class
        .getName()).log(java.util.logging.Level.SEVERE, null, ex);

    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
      java.util.logging.Logger.getLogger(Gui.class
        .getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    //</editor-fold>

    FileSystem este = new FileSystem(3, 5);
    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        new Gui(este).setVisible(true);
      }
    });
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton BCopyVR;
  private javax.swing.JButton BCopyVV;
  private javax.swing.JButton BModificar;
  private javax.swing.JButton BMover;
  private javax.swing.JButton ButtonAnalize1;
  private javax.swing.JLabel LabelPath;
  private javax.swing.JLabel LabelPathW;
  private javax.swing.JLabel clipboard;
  private javax.swing.JButton jButton1;
  private javax.swing.JButton jButton2;
  private javax.swing.JButton jButton3;
  private javax.swing.JButton jButton4;
  private javax.swing.JButton jButton5;
  private javax.swing.JButton jButton6;
  private javax.swing.JButton jButton9;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JButton limpiar;
  private javax.swing.JList<String> lista;
  private javax.swing.JTree log;
  // End of variables declaration//GEN-END:variables
}
