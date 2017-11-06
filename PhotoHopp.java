import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import javax.swing.*;

public class PhotoHopp {
    public static void main(String[] args) throws IOException {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image srcImage1 = toolkit.getImage("river.gif");
        Image srcImage2 = toolkit.getImage("fki_start.jpg");
        Image srcImage3 = toolkit.getImage("farbenkreis_b.gif");


        ImageFilter colorfilter = new TestFilter();
        Image filteredImage1 = toolkit.createImage(
            new FilteredImageSource(srcImage1.getSource(),colorfilter));
        Image filteredImage2 = toolkit.createImage(
            new FilteredImageSource(srcImage2.getSource(),colorfilter));
        Image filteredImage3 = toolkit.createImage(
                new FilteredImageSource(srcImage3.getSource(),colorfilter));
        
        JFrame frame = new JFrame("Images and pixelwise filtering");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(Color.RED);  
        Panel allImagesPanel = new Panel();
        allImagesPanel.setLayout(new GridLayout(0,3));
        allImagesPanel.add(new ImagePanel(srcImage1));
        allImagesPanel.add(new ImagePanel(srcImage2));
        allImagesPanel.add(new ImagePanel(srcImage3));
        allImagesPanel.add(new ImagePanel(filteredImage1));
        allImagesPanel.add(new ImagePanel(filteredImage2));
        allImagesPanel.add(new ImagePanel(filteredImage3));
        frame.getContentPane().add(allImagesPanel);
        frame.setBounds(50,50,1000,500);
        frame.setVisible(true);          
     }
}

class ImagePanel extends JPanel  {
    private Image image;

    public ImagePanel(Image image)  {
        this.image = image;
    }
    
    public void paintComponent(Graphics g) {   
        super.paintComponent(g);
//        System.out.println("paintComponent" + this + image.getWidth(this) + " " + image.getHeight(this));
        g.drawImage(image, 10, 10,image.getWidth(this) , image.getHeight(this), this); 
    }
}
    
    

class TestFilter extends RGBImageFilter {
    
    /**
    Aufgabe j), Weitere Spielereien
    Für FILTER_RAND , nicht unbedingt relevant für die Lösung
    */
    public int last_r, last_g, last_b;
    TestFilter() {
        last_r = 268911827; last_g = 138911384; last_b = 131449373;
    }
    
    /**
    Diese Funktion filterRGB wird für jedes Pixel jeden Bildes aufgerufen und bekommt als Argumente
    einmal das originale Pixel über die Variable pixel übergeben und die Information welches Pixel gerade 
    bearbeitet wird über seine x- und y-Koordianten und das über die Variablen x und y.
    Als Rückgabewert gibt sie das (bearbeitete) Pixel, das das neue (bearbeitete) Bild erhält.
    */
    public int filterRGB(int x, int y, int pixel) {
        /**
        Maximale und minimale Zahl sowie Hälfte, die ein (unsigned) byte speichern kann.
        Nicht unbedingt notwendig sie veränderbar, also wie hier als Variable zu
        definieren, aber damit kann man z.B für Aufgabe d) den Schwellenwert, ab dem ein Pixel
        als hell erkannt werden soll einstellen. (FILTER_BYTE_HALF)
        */
        final int FILTER_BYTE_MAX = 255, 
        FILTER_BYTE_MIN = 0, 
        FILTER_BYTE_HALF = 127;
       
        /**
        Und hier werden ein paar Konstanten definiert, damit wir sprechende Namen in der switch-case haben und nicht bloß
        Zahlen.
        */
       
        final byte FILTER_NONE = 0, /* do nothing */
        FILTER_PAINT = 1, /* paint over whole image with color */
        FILTER_COLORS = 2, /* filter colors */
        FILTER_SWAP_RB = 3, /* swap red and blue */
        FILTER_FIRST_BYTE = 4, /* "rumspielen" */
        FILTER_BRIGHTNESS = 5, /* correct brightness */
        FILTER_GREY = 6, /* transform image to monochrome */
        FILTER_BW = 7, /* transform to black and white only */
        FILTER_NEG = 8, /* ~? */
        FILTER_SHOW_RECT = 9, /* render only rectangular part of image */
        FILTER_SHOW_B_ONLY = 10, /* render only parts of image with brightness over 50% */
        FILTER_SHOW_D_ONLY = 11, /* render only parts of image with brightness under 50% */
        FILTER_ADD_NOISE = 12, /* add "karo" noise */ 
        FILTER_XOR_RB = 13, /* XOR colors */
        FILTER_RAND = 14; /* fill with random */
       
        /**
        Wie in der Aufgabenstellung gefordert, eine Schaltervariable
        */
        byte filter_switch = FILTER_RAND; /* switch */
       
        /**
        Für Aufgabe i) 1, Größe des Rechtecks, das angezeigt werden soll
        */
        int rect_height = 120;
        int rect_bright = 60;
       
        /**
        Die Variable pixel enthält den Rot-, Grün-, Blau- sowie Transparenz-Wert.
        Hier werden die einzelnen Bestandteile der Variable pixel auf mehrere aufgeteilt,
        da sie einzeln bearbeitet werden. 
       
        Hier eine Veranschaulichung der Variable pixel: 
        (höchstwertiges Byte ist als erstes dargestellt, nicht die Reihenfolge im Speicher)
       
        00000000 00000000 00000000 00000000  -> alle 32bits bilden die Variable pixel
            |        |        |        |
        px_op     px_r     px_g     px_b
        Trans-    Rot-    Grün-     Blau-
        parenz-   Wert    Wert      Wert
         Wert
      
        -> die einzelnen px_* Variablen sind 8 bit (1 byte ) groß, die möglichen Werte 0 bis 255
        stehen für die Helligkeit des jeweiligen Farbwerts, 0 ist komplett dunkel (schwarz),
        255 maximal hell.
       
        Nun wird pixel mittels AND und Bitshifting getrennt.
        Dabei werden mittels AND die anderen Bits für die Farb- oder Transparenz-Anteile auf 
        0 gesetzt und dann die verbliebenen 8bits auf das kleinstwertige Byte verschoben,
        also nachdem sich z.B der Rot-Wert 2bytes weiter links vom kleinstwertigen Byte 
        befindet wird es 2 * 8bits = 16 bits nach rechts verschoben.
       
        -> px_r = (pixel & 0x00ff0000) >> 16;
       
        Wie in Aufgabenstellung gefordert, wird dies vor der switch-case gemacht,
        um Code-Duplikation zu vermeiden, da es mehrmals gebraucht wird.
        */
        int px_op = (pixel & 0xff000000) >> 24; 
        int px_r =  (pixel & 0x00ff0000) >> 16; 
        int px_g =  (pixel & 0x0000ff00) >> 8; 
        int px_b =   pixel & 0x000000ff;
       
        /**
        Mittelwert der drei Farbwerte.
        */
        int avg = ( px_r + px_g + px_b ) / 3;
       
        /**
        Wenn die Durchschnittshelligkeit der drei Farbwerte größer als 
        FILTER_BYTE_HALF ist, dann ist das Pixel hell und somit 
        die Variable bright true
        */
        boolean bright = avg > FILTER_BYTE_HALF;
       
        switch ( filter_switch ) {
            case FILTER_NONE:break;
           
            /**
            Aufgabe a)
            Aus dem vorgegeben Farbwert ff807e03 sind die einzelnen 1byte großen Stellen
            einfach der Reihenfolge nach abzulesen und abzuschreiben.
            */
            case FILTER_PAINT:
                px_op = 0xff; px_r = 0x80; px_g = 0x7e; px_b = 0x03; 
                break;
           
            /**
            Aufgabe b)
            Damit nur ein Farbanteil sichtbar ist werden die restlichen 2 auf 0 gesetzt und 
            somit unsichtbar (schwarz) gemacht.
            */
            case FILTER_COLORS:
                px_r = 0; px_b = 0;  /* green only */
                //px_g = 0; px_b = 0; /* red only */
                //px_r = 0; px_g = 0; /* blue only */
                break;
           /**
           Aufgabe c)
           Um Rot und Blau zu vertauschen wird zuerst in tmp_red der alte Rot-Wert
           geschpeichert, um dann mit dem Blau-Wert überschrieben zu werden,
           anschließend wird der Blau-Wert mit dem alten Rot Wert überschrieben.
           */
           case FILTER_SWAP_RB:
                int tmp_red = px_r;
                px_r = px_b; px_b = tmp_red;
                break;
           
           /**
           Aufgabe c)
           Um nur die hellen Pixel anzuzeigen, wird überprüft ob das Pixel hell ist
           und wenn ja, wird jeder Farbwert beibehalten, ansonsten wird er auf FILTER_BYTE_MIN
           ( default ist 0, also schwarz ) gesetzt.
           
           Ternary-operator wird statt if-else-case benutzt.
           
           Bei der unteren case ( FILTER_SHOW_D_ONLY ) wird nur wenn das pixel nicht hell ist
           beibehalten, ansonsten auf FILTER_BYTE_MIN gesetzt, um so nur die dunkelen Pixel anzuzeigen
           
           */
           case FILTER_SHOW_B_ONLY:
                px_r = bright ? px_r : FILTER_BYTE_MIN;
                px_g = bright ? px_g : FILTER_BYTE_MIN;
                px_b = bright ? px_b : FILTER_BYTE_MIN;
                break;
           case FILTER_SHOW_D_ONLY:
                px_r = bright ? FILTER_BYTE_MIN : px_r;
                px_g = bright ? FILTER_BYTE_MIN : px_g;
                px_b = bright ? FILTER_BYTE_MIN : px_b;
                break;
            
           /**
           Aufgabe e)
           Das Bild wird halb transparent
           */
           case FILTER_FIRST_BYTE:
                px_op = 127; 
                break;
           
           /**
           Aufgabe f)
           Jeder Farbwert wird mit dem einstellbaren 
           brightness Wert in Prozent multipliziert um die 
           Helligkeit einzustellen. Dann werden die Farbanteile auf 255 begrenzt.
           
           Optional kann das natürlich nur auf manche Farbanteile gemacht werden.
           */
           case FILTER_BRIGHTNESS:
                float brightness = 50.0f; 
                int 
                tmp_r = (int)(px_r * brightness / 100.0f ), 
                tmp_g = (int)(px_g * brightness / 100.0f ), 
                tmp_b = (int)(px_b * brightness / 100.0f );
            
                px_r  = tmp_r > FILTER_BYTE_MAX ? FILTER_BYTE_MAX : tmp_r; 
                px_g  = tmp_g > FILTER_BYTE_MAX ? FILTER_BYTE_MAX : tmp_g; 
                px_b  = tmp_b > FILTER_BYTE_MAX ? FILTER_BYTE_MAX : tmp_b;                      
                break;
           
           /**
           Aufgabe g)
           Damit keine Farbe hervorsticht werden alle Farbanteile mit dem gleichen 
           Durchschnittswert überschrieben, dabei entsteht das Grauwertbild.
           */
           case FILTER_GREY:
                px_r = px_g = px_b = avg;      
                break;
           /**
           Aufgabe g)
           Damit keine Farbe hervorsticht werden wieder alle Farbanteile mit dem gleichen 
           Wert überschrieben, hierbei wird jedoch abhängig davon, ob das Pixel hell ist 
           oder nicht, es komplett dunkel oder komplett hell gemacht.
           Dabei entsteht das Schwarz-Weiß-Bild.
           */
           case FILTER_BW:
                px_r = px_g = px_b = ( bright ? FILTER_BYTE_MAX : FILTER_BYTE_MIN );
                break;
           
           /**
           Aufgabe h)
           Auf jeden Farbwert wird der Negieren-operator angewendet, 
           also wird z.B die Zahl (binär)  00101010 zu 11010101,
           dabei entsteht das Negativ des Bildes.
           */
           case FILTER_NEG:
                px_r = ~px_r; px_g = ~px_g; px_b = ~px_b;
                break;
                
           /**
           Aufgabe i) 1
           Zeige nur Ausschnitt aus dem Bild.
           Nachdem die Position des aktuell bearbeiteten Pixels bekannt ist, 
           können wir überprüfen, ob diese größer ist als den Ausschnitt den wir 
           haben möchten und wenn ja, wird die Opazität dieses Pixels auf FILTER_BYTE_MIN
           gesetzt, das pixel wird also maximal transparent und somit unsichtbar.
           */
           case FILTER_SHOW_RECT:
                px_op = ( ( x > rect_bright || y > rect_height ) ? FILTER_BYTE_MIN : px_op);    
                break;
                
           /**
           Aufgabe i) 2
           Karo Muster hinzufügen.
           Bei ungeraden Zeilennummern und geraden Spaltennummern oder genau umgekehrt wird das
           Pixel über den Transparenzwert unsichtbar gemacht.
           
           Geradzahligkeit wird über Modulo-Operation überprüft:
           Wenn die Zahl gerade ist, ergibt Division durch 2 keinen Rest.
           */
           case FILTER_ADD_NOISE:
                px_op = ( (y%2 != 0 && x%2 == 0) || (y%2 == 0 && x%2 != 0) ? FILTER_BYTE_MIN : px_op );
                break;
           
           /**
           Aufgabe j) 
           Weitere Spielereien: XOR.
           */
           case FILTER_XOR_RB:
                px_r = px_r ^ px_g;
                px_g = px_g ^ px_b;
                px_b = px_b ^ px_r;
                
                /*px_r = px_r ^ px_b;
                px_g = px_g ^ px_r;
                px_b = px_b ^ px_g;*/
                break;
                
           /**
           Aufgabe j) 
           Weitere Spielereien: fülle Bild mit Zufall.
           */
           case FILTER_RAND:
                final int sh1 = 5, sh2 = 9, sh3 = 13; 
                // LCG
                px_r = (px_r + last_g) * 69069 + 161551;
                px_g = (px_g + last_b) * 49567 + 271357;
                px_b = (px_b + last_r) * 18963 + 131351;
                          
                // ROR
                px_r ^= (last_r >>> sh1) | (last_r << 32 - sh1);
                px_g ^= (last_g >>> sh2) | (last_g << 32 - sh2);
                px_b ^= (last_b >>> sh3) | (last_b << 32 - sh3);
                
                last_r = px_r; last_g = px_g; last_b = px_b;
                
                break;
                
   
           default: break;
       }
       
       /**
       Hier werden die einzelnen bytes an ihren Positionen verschoben
       und mittels OR zu einem Integer "zusammengebaut".
       
       Vorher werden alle Bits der einzelnen px_* Variablen außer die im niedrigstwertigsten byte auf 0 gesetzt,
       da die Variablen der einzelnen Farbwerte den Datentyp int haben, also 4byte groß sind und z.B 
       bei FILTER_NEG, also nach der Anwendung des Negieren-operator, auch die oberen bits modifiziert werden
       und somit mehr Bits als gewünscht gesetzt werden würden.
       */
       
       return (((px_op & 0xff) << 24) | ((px_r & 0xff) << 16) | ((px_g & 0xff) << 8) | (px_b & 0xff));
    }
}
