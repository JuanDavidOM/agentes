

package com.agentes;

import jade.core.AID;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class GuiVentaLibros extends JFrame {	
	private AgenteVendedorLibros miAgente;
	
	private JTextField CampoTitulo, CampoPrecio;
	
	GuiVentaLibros(AgenteVendedorLibros a) {
		super(a.getLocalName());
		
		miAgente = a;
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 2));
		p.add(new JLabel("Tarea:")); //titulo
		CampoTitulo = new JTextField(15);
		p.add(CampoTitulo);
		p.add(new JLabel("Feromona:")); //precio
		CampoPrecio = new JTextField(15);
		p.add(CampoPrecio);
		getContentPane().add(p, BorderLayout.CENTER);
		
		JButton addButton = new JButton("Agregar");
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					String title = CampoTitulo.getText().trim();
					String price = CampoPrecio.getText().trim();
					miAgente.ActualizaCatalogo(title, Integer.parseInt(price));
					CampoTitulo.setText("");
					CampoPrecio.setText("");
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(GuiVentaLibros.this, "Valor Invalido. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		} );
		p = new JPanel();
		p.add(addButton);
		getContentPane().add(p, BorderLayout.SOUTH);
		
		// Make the agent terminate when the user closes 
		// the GUI using the button on the upper right corner	
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				miAgente.doDelete();
			}
		} );
		
		setResizable(false);
	}
	
	public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}	
}
