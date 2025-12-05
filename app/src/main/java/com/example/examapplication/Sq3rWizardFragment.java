package com.example.examapplication;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Sq3rWizardFragment extends Fragment {

    private TextView phaseTextView;
    private Button primaryButton;      // Continua / Salva
    private Button addQuestionButton;  // Aggiungi domanda
    private LinearLayout dynamicContainer;

    private int currentPhase = 0; // 0=Survey, 1=Question, 2=Read, 3=Recite
    private final List<String> questions = new ArrayList<>();
    private final List<EditText> answerEditTexts = new ArrayList<>();

    public Sq3rWizardFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sq3r_wizard, container, false);

        phaseTextView     = view.findViewById(R.id.phaseTextView);
        primaryButton     = view.findViewById(R.id.primaryButton);
        addQuestionButton = view.findViewById(R.id.addQuestionButton);
        dynamicContainer  = view.findViewById(R.id.dynamicContainer);

        setupPhaseSurvey();

        primaryButton.setOnClickListener(v -> onPrimaryButtonClick());
        addQuestionButton.setOnClickListener(v -> onAddQuestionClick());

        return view;
    }

    // ---------- NAVIGAZIONE FASI ----------

    private void onPrimaryButtonClick() {
        switch (currentPhase) {
            case 0:
                setupPhaseQuestion();
                break;
            case 1:
                setupPhaseRead();
                break;
            case 2:
                setupPhaseRecite();
                break;
            case 3:
                saveQuestionsAndAnswersToPdf();
                break;
        }
    }

    private void onAddQuestionClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Nuova domanda");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String q = input.getText().toString().trim();
            if (!q.isEmpty()) {
                questions.add(q);
                refreshQuestionListUI();
            }
        });
        builder.setNegativeButton("Annulla", null);
        builder.show();
    }

    // ---------- FASI SQ3R ----------

    private void setupPhaseSurvey() {
        currentPhase = 0;
        dynamicContainer.removeAllViews();
        addQuestionButton.setVisibility(View.GONE);
        primaryButton.setText("Continua");

        phaseTextView.setText(
                "In primo luogo, esaminate il materiale: se si tratta di un capitolo " +
                        "scorrete con lo sguardo i suoi contenuti: l’introduzione, i vari paragrafi " +
                        "e il capitolo finale, al fine di avere un quadro d’insieme generale."
        );
    }

    private void setupPhaseQuestion() {
        currentPhase = 1;
        dynamicContainer.removeAllViews();
        answerEditTexts.clear();

        phaseTextView.setText(
                "Fase Question:\n" +
                        "Annota tutte le domande che ti vengono in mente " +
                        "su ciò che vuoi capire da questo testo."
        );
        addQuestionButton.setVisibility(View.VISIBLE);
        primaryButton.setText("Continua");

        refreshQuestionListUI();
    }

    private void refreshQuestionListUI() {
        dynamicContainer.removeAllViews();
        for (String q : questions) {
            TextView tv = new TextView(getActivity());
            tv.setText("- " + q);
            tv.setPadding(0, 8, 0, 8);
            dynamicContainer.addView(tv);
        }
    }

    private void setupPhaseRead() {
        currentPhase = 2;
        dynamicContainer.removeAllViews();
        answerEditTexts.clear();
        addQuestionButton.setVisibility(View.GONE);

        phaseTextView.setText(
                "Fase Read:\n" +
                        "Leggete attentamente e con cura le parti contenenti le informazioni rilevanti. " +
                        "Lo scopo della lettura deve essere la ricerca delle risposte alle vostre " +
                        "domande formulate prima. Focalizzatevi sui punti salienti."
        );
        primaryButton.setText("Continua");
    }

    private void setupPhaseRecite() {
        currentPhase = 3;
        dynamicContainer.removeAllViews();
        answerEditTexts.clear();
        addQuestionButton.setVisibility(View.GONE);

        phaseTextView.setText(
                "Fase Recite:\n" +
                        "Per ogni domanda, rispondi con parole tue e collega alle conoscenze che hai già."
        );
        primaryButton.setText("Salva in PDF");

        for (String q : questions) {
            TextView tvQ = new TextView(getActivity());
            tvQ.setText(q);
            tvQ.setPadding(0, 16, 0, 4);
            dynamicContainer.addView(tvQ);

            EditText etA = new EditText(getActivity());
            etA.setMinLines(2);
            etA.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
            etA.setHint("Risposta...");
            dynamicContainer.addView(etA);

            answerEditTexts.add(etA);
        }
    }

    // ---------- SALVATAGGIO PDF ----------

    private void saveQuestionsAndAnswersToPdf() {
        StringBuilder sb = new StringBuilder();
        sb.append("Metodo SQ3R - Domande e risposte\n\n");

        for (int i = 0; i < questions.size(); i++) {
            String q = questions.get(i);
            String a = "";
            if (i < answerEditTexts.size() && answerEditTexts.get(i) != null) {
                a = answerEditTexts.get(i).getText().toString();
            }
            sb.append("Domanda ").append(i + 1).append(": ").append(q).append("\n");
            sb.append("Risposta: ").append(a).append("\n\n");
        }

        saveToPdf(sb.toString());
    }

    private void saveToPdf(String content) {
        PdfDocument pdf = new PdfDocument();
        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4
        PdfDocument.Page page = pdf.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        Paint titlePaint = new Paint();
        titlePaint.setTextSize(18f);
        titlePaint.setFakeBoldText(true);

        Paint bodyPaint = new Paint();
        bodyPaint.setTextSize(14f);

        int x = 40;
        int y = 60;

        // Titolo
        canvas.drawText("Metodo SQ3R - Domande e risposte", x, y, titlePaint);
        y += 40;

        for (String line : content.split("\n")) {
            if (y > 800) break; // semplice: una pagina
            canvas.drawText(line, x, y, bodyPaint);
            y += 22;
        }

        pdf.finishPage(page);

        String fileName = "SQ3R_" + System.currentTimeMillis() + ".pdf";

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");

        Uri uri = requireContext().getContentResolver()
                .insert(MediaStore.Files.getContentUri("external"), values);

        try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
            pdf.writeTo(os);
            showMessage("PDF creato", "Salvato come " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Errore", "Impossibile salvare il PDF");
        } finally {
            pdf.close();
        }
    }

    private void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
