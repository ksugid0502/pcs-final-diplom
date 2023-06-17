import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    private Map<String, List<PageEntry>> answers;

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        if (!pdfsDir.isDirectory())
            throw new IllegalArgumentException("Not a directory");

        answers = new HashMap<>();

        for (var file: pdfsDir.listFiles()) {
            var pdf = new PdfDocument(new PdfReader(file));

            for (int pageNumber = 1; pageNumber <= pdf.getNumberOfPages(); pageNumber++) {
                loadWordsFromPage(pdf, file.getName(), pageNumber);
            }
        }
    }

    private void loadWordsFromPage(PdfDocument pdf, String documentName, int pageNumber) {
        var page = pdf.getPage(pageNumber);
        var text = PdfTextExtractor.getTextFromPage(page);
        var words = text.split("\\P{IsAlphabetic}+");

        Map<String, Integer> freqs = new HashMap<>();
        for (var word: words) {
            if (word.isEmpty()) {
                continue;
            }
            word = word.toLowerCase();
            freqs.put(word, freqs.getOrDefault(word, 0) + 1);
        }

        freqs.forEach((word, count) -> {
            var pageEntry = new PageEntry(documentName, pageNumber, count);
            var list = answers.putIfAbsent(word, new ArrayList<>(List.of(pageEntry)));
            if (list != null) {
                list.add(pageEntry);
            }
        });
    }

    @Override
    public List<PageEntry> search(String word) {
        var list = new ArrayList<>(answers.getOrDefault(word.toLowerCase(), new ArrayList<>()));
        list.sort(Comparator.reverseOrder());
        return list;
    }
}
