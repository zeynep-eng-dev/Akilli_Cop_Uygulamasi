import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

// Bir çöp kutusu: konum (x, y) ve içindeki çöpün ağırlığı (kg)

class CopKutusu {
    int id, x, y;
    double agirlik;

    CopKutusu(int id, int x, int y, double agirlik) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.agirlik = agirlik;
    }

    // Hafif / Orta / Dolu sınıflandırması (limite göre)
    String durum(double limit) {
        if (agirlik >= limit) return "DOLU";
        if (agirlik >= limit / 2) return "ORTA";
        return "HAFIF";
    }

    Color renk(double limit) {
        switch (durum(limit)) {
            case "DOLU": return new Color(220, 50, 50);
            case "ORTA": return new Color(240, 160, 0);
            default:     return new Color(46, 160, 67);
        }
    }
}

// ---------------------------------------------------------------
// Bir çöp toplama aracı
// ---------------------------------------------------------------
class Arac {
    int id;
    List<CopKutusu> atananlar = new ArrayList<>(); // bu araca verilen dolu kutular
    List<Point> yol = new ArrayList<>();            // haritada çizilecek rota
    List<String> adimlar = new ArrayList<>();       // ekrana yazılacak adımlar
    double yuk = 0, toplamMesafe = 0, toplamCop = 0;
    int sefer = 1;

    Arac(int id) {
        this.id = id;
    }
}

// Harita penceresi (Java Swing)

class HaritaPanel extends JPanel {
    static final int MARGIN = 40;
    static final int OLCEK = 12; // 1 birim = 12 piksel
    static final Color[] ARAC_RENK = {
        new Color(37, 99, 235), new Color(147, 51, 234),
        new Color(13, 148, 136), new Color(219, 39, 119), new Color(120, 113, 108)
    };

    List<CopKutusu> kutular;
    List<Arac> araclar;
    double limit;

    HaritaPanel(List<CopKutusu> kutular, List<Arac> araclar, double limit) {
        this.kutular = kutular;
        this.araclar = araclar;
        this.limit = limit;
        setPreferredSize(new Dimension(920, 680));
        setBackground(Color.WHITE);
    }

    int px(int x) { return MARGIN + x * OLCEK; }
    int py(int y) { return MARGIN + (50 - y) * OLCEK; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Izgara
        g2.setColor(new Color(235, 235, 235));
        for (int i = 0; i <= 50; i += 10) {
            g2.drawLine(px(i), py(0), px(i), py(50));
            g2.drawLine(px(0), py(i), px(50), py(i));
        }
        g2.setColor(Color.GRAY);
        g2.drawRect(px(0), py(50), 50 * OLCEK, 50 * OLCEK);

        // Araç rotaları
        g2.setStroke(new BasicStroke(2.5f));
        for (Arac a : araclar) {
            g2.setColor(ARAC_RENK[(a.id - 1) % ARAC_RENK.length]);
            for (int i = 0; i < a.yol.size() - 1; i++) {
                Point p1 = a.yol.get(i), p2 = a.yol.get(i + 1);
                g2.drawLine(px(p1.x), py(p1.y), px(p2.x), py(p2.y));
            }
        }

        // Çöp kutuları
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        for (CopKutusu k : kutular) {
            g2.setColor(k.renk(limit));
            g2.fillOval(px(k.x) - 8, py(k.y) - 8, 16, 16);
            g2.setColor(Color.BLACK);
            g2.drawString(String.valueOf(k.id), px(k.x) + 10, py(k.y) - 6);
        }

        // Depo
        g2.setColor(Color.BLACK);
        g2.fillRect(px(Main.DEPO_X) - 10, py(Main.DEPO_Y) - 10, 20, 20);
        g2.setColor(Color.WHITE);
        g2.drawString("D", px(Main.DEPO_X) - 4, py(Main.DEPO_Y) + 4);

        // Sağ taraftaki açıklama (legend)
        int lx = 680, ly = 60;
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("Akıllı Çöp Toplama", lx, ly);
        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        ly += 30;
        String[] adlar = {"Dolu (toplanacak)", "Orta", "Hafif"};
        Color[] renkler = {new Color(220, 50, 50), new Color(240, 160, 0), new Color(46, 160, 67)};
        for (int i = 0; i < 3; i++) {
            g2.setColor(renkler[i]);
            g2.fillOval(lx, ly - 11, 14, 14);
            g2.setColor(Color.BLACK);
            g2.drawString(adlar[i], lx + 22, ly);
            ly += 24;
        }
        g2.setColor(Color.BLACK);
        g2.fillRect(lx, ly - 11, 14, 14);
        g2.drawString("Depo", lx + 22, ly);
        ly += 40;
        for (Arac a : araclar) {
            g2.setColor(ARAC_RENK[(a.id - 1) % ARAC_RENK.length]);
            g2.fillRect(lx, ly - 8, 22, 5);
            g2.setColor(Color.BLACK);
            g2.drawString("Araç " + a.id + ": " + Math.round(a.toplamMesafe) + " birim", lx + 30, ly);
            ly += 22;
            g2.drawString("   " + a.sefer + " sefer, " + a.toplamCop + " kg", lx + 30, ly);
            ly += 28;
        }
    }
}
// Ana program

public class Main {
    static final int DEPO_X = 25, DEPO_Y = 25; // depo haritanın ortasında

    static double mesafe(int x1, int y1, int x2, int y2) {
        int dx = x1 - x2, dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // CSV dosyasından kutuları oku (format: id,x,y,agirlik)
    static List<CopKutusu> csvOku(String dosyaAdi) {
        List<CopKutusu> liste = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dosyaAdi), "UTF-8"))) {
            String satir;
            while ((satir = br.readLine()) != null) {
                String[] p = satir.trim().split(",");
                if (p.length < 4) continue;
                try {
                    liste.add(new CopKutusu(Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()),
                            Integer.parseInt(p[2].trim()), Double.parseDouble(p[3].trim())));
                } catch (NumberFormatException e) {
                    // başlık satırı veya bozuk satır, atla
                }
            }
        } catch (IOException e) {
            System.out.println("Dosya okunamadı: " + e.getMessage());
        }
        return liste;
    }

    static List<CopKutusu> rastgeleKutular(int adet) {
        Random random = new Random();
        List<CopKutusu> liste = new ArrayList<>();
        for (int i = 1; i <= adet; i++) {
            liste.add(new CopKutusu(i, random.nextInt(51), random.nextInt(51), random.nextInt(51)));
        }
        return liste;
    }

    // Bir aracın rotasını planla: skor = ağırlık / (mesafe + 1)
    static void planla(Arac a, double kapasite) {
        List<CopKutusu> kalan = new ArrayList<>(a.atananlar);
        int cx = DEPO_X, cy = DEPO_Y;
        a.yol.add(new Point(cx, cy));

        while (!kalan.isEmpty()) {
            CopKutusu secilen = null;
            double enIyiSkor = -1;
            for (CopKutusu k : kalan) {
                if (a.yuk + k.agirlik <= kapasite) {
                    double skor = k.agirlik / (mesafe(cx, cy, k.x, k.y) + 1);
                    if (skor > enIyiSkor) {
                        enIyiSkor = skor;
                        secilen = k;
                    }
                }
            }

            if (secilen == null) {
                if (a.yuk == 0) {
                    // Boş araca bile sığmıyor, atla
                    CopKutusu k = kalan.remove(0);
                    a.adimlar.add("Kutu " + k.id + " (" + k.agirlik + " kg) kapasiteden büyük, atlandı!");
                    continue;
                }
                a.adimlar.add("Araç doldu (" + a.yuk + " kg). Depoya dön ve boşalt.");
                a.toplamMesafe += mesafe(cx, cy, DEPO_X, DEPO_Y);
                cx = DEPO_X;
                cy = DEPO_Y;
                a.yol.add(new Point(cx, cy));
                a.yuk = 0;
                a.sefer++;
                continue;
            }

            a.toplamMesafe += mesafe(cx, cy, secilen.x, secilen.y);
            cx = secilen.x;
            cy = secilen.y;
            a.yol.add(new Point(cx, cy));
            a.yuk += secilen.agirlik;
            a.toplamCop += secilen.agirlik;
            a.adimlar.add("Sefer " + a.sefer + ": Kutu " + secilen.id + "'e git ("
                    + secilen.agirlik + " kg) | yük: " + a.yuk + " kg");
            kalan.remove(secilen);
        }
        // İş bitti, depoya dön
        if (cx != DEPO_X || cy != DEPO_Y) {
            a.toplamMesafe += mesafe(cx, cy, DEPO_X, DEPO_Y);
            a.yol.add(new Point(DEPO_X, DEPO_Y));
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Toplama limiti (kg): ");
        double limit = Double.parseDouble(scanner.nextLine().trim().replace(',', '.'));
        System.out.print("Araç kapasitesi (kg): ");
        double kapasite = Double.parseDouble(scanner.nextLine().trim().replace(',', '.'));
        System.out.print("Araç sayısı (1-5): ");
        int aracSayisi = Integer.parseInt(scanner.nextLine().trim());
        if (aracSayisi < 1) aracSayisi = 1;
        if (aracSayisi > 5) aracSayisi = 5;
        System.out.print("CSV dosya adı (boş bırakırsan rastgele kutu üretilir): ");
        String dosya = scanner.nextLine().trim();

        List<CopKutusu> kutular = dosya.isEmpty() ? rastgeleKutular(20) : csvOku(dosya);
        if (kutular.isEmpty()) {
            System.out.println("Kutu bulunamadı, rastgele 20 kutu üretiliyor.");
            kutular = rastgeleKutular(20);
        }
        // --- Rapor: kutu durumları ---
        int hafif = 0, orta = 0, dolu = 0;
        System.out.println("\n--- Tüm çöp kutuları ---");
        List<CopKutusu> toplanacak = new ArrayList<>();
        for (CopKutusu k : kutular) {
            String d = k.durum(limit);
            System.out.println("Kutu " + k.id + " | (" + k.x + "," + k.y + ") | " + k.agirlik + " kg | " + d);
            if (d.equals("DOLU")) { dolu++; toplanacak.add(k); }
            else if (d.equals("ORTA")) orta++;
            else hafif++;
        }
        System.out.println("\nHafif: " + hafif + " | Orta: " + orta + " | Dolu: " + dolu);

        // --- Kutuları araçlara bölge (açı) bazlı dağıt ---
        List<Arac> araclar = new ArrayList<>();
        for (int i = 1; i <= aracSayisi; i++) araclar.add(new Arac(i));
        for (CopKutusu k : toplanacak) {
            double aci = Math.atan2(k.y - DEPO_Y, k.x - DEPO_X) + Math.PI; // 0 .. 2*PI
            int bolge = (int) (aci / (2 * Math.PI) * aracSayisi);
            if (bolge >= aracSayisi) bolge = aracSayisi - 1;
            araclar.get(bolge).atananlar.add(k);
        }
        // --- Her araç için rota planla ve yazdır ---
        double genelMesafe = 0, genelCop = 0;
        for (Arac a : araclar) {
            System.out.println("\n=== Araç " + a.id + " (" + a.atananlar.size() + " kutu) ===");
            planla(a, kapasite);
            if (a.adimlar.isEmpty()) System.out.println("Bu araca görev düşmedi.");
            for (String s : a.adimlar) System.out.println(s);
            genelMesafe += a.toplamMesafe;
            genelCop += a.toplamCop;
        }
        System.out.println("\n--- Genel özet ---");
        for (Arac a : araclar) {
            System.out.println("Araç " + a.id + ": " + a.sefer + " sefer, " + a.toplamCop + " kg, "
                    + Math.round(a.toplamMesafe) + " birim");
        }
        System.out.println("TOPLAM: " + genelCop + " kg çöp, " + Math.round(genelMesafe) + " birim yol");

        // --- Haritayı çiz ---
        final List<CopKutusu> kutularSon = kutular;
        final double limitSon = limit;
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("(Bu ortamda pencere açılamıyor.)");
        } else {
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Akıllı Çöp Toplama Haritası");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new HaritaPanel(kutularSon, araclar, limitSon));
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            });
        }
        scanner.close();
    }
}
