package com.example.examapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    public static class StudyEntry {
        public long timestamp;
        public float hours;
        public boolean isLegacy;

        public StudyEntry() { }
        public StudyEntry(long ts, float h) {
            timestamp = ts;
            hours = h;
            isLegacy = false;
        }
    }

    private LineChart weeklyChart;
    private Spinner subjectSpinner;
    private Button addSubjectButton, addHoursButton, editEntriesButton;

    private Map<String, List<StudyEntry>> studyHoursPerSubject = new HashMap<>();
    private ArrayAdapter<String> subjectAdapter;
    private String selectedSubject = null;

    private FirebaseFirestore db;
    private FirebaseUser user;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        weeklyChart = view.findViewById(R.id.weeklyChart);
        subjectSpinner = view.findViewById(R.id.subjectSpinner);
        addSubjectButton = view.findViewById(R.id.addSubjectButton);
        addHoursButton = view.findViewById(R.id.addHoursButton);
        editEntriesButton = view.findViewById(R.id.editEntriesButton);

        subjectAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);

        subjectSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedSubject = subjectAdapter.getItem(position);
                updateWeeklyChart();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {

            }
        });

        addSubjectButton.setOnClickListener(v -> showAddSubjectDialog());
        addHoursButton.setOnClickListener(v -> showAddHoursDialog());
        editEntriesButton.setOnClickListener(v -> showEditEntriesDialog());

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (user != null) {
            loadDataFromFirebase();
        }

        return view;
    }

    private void showAddSubjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Nuova materia");

        final EditText input = new EditText(requireContext());
        input.setHint("Nome materia");
        builder.setView(input);

        builder.setPositiveButton("Aggiungi", (dialog, which) -> {
            String subject = input.getText().toString().trim();
            if (!subject.isEmpty() && !studyHoursPerSubject.containsKey(subject)) {
                studyHoursPerSubject.put(subject, new ArrayList<>());
                subjectAdapter.add(subject);
                subjectAdapter.notifyDataSetChanged();
                if (subjectAdapter.getCount() == 1) {
                    subjectSpinner.setSelection(0);
                }
                saveDataToFirebase();
            }
        });

        builder.setNegativeButton("Annulla", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAddHoursDialog() {
        if (selectedSubject == null) return;

        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);

                    if (existsEntryForDate(selectedSubject, calendar.getTimeInMillis())) {
                        Toast.makeText(requireContext(), "Dati già inseriti per questa data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Ore il " + dateFormatter.format(calendar.getTime()) + " per " + selectedSubject);

                    final EditText input = new EditText(requireContext());
                    input.setHint("Ore");
                    input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                    builder.setView(input);

                    builder.setPositiveButton("Aggiungi", (dialog2, which2) -> {
                        try {
                            float val = Float.parseFloat(input.getText().toString());
                            List<StudyEntry> oreList = studyHoursPerSubject.get(selectedSubject);
                            if (oreList != null) {
                                oreList.add(new StudyEntry(calendar.getTimeInMillis(), val));
                                saveDataToFirebase();
                                updateWeeklyChart();
                            }
                        } catch (Exception e) { }
                    });

                    builder.setNegativeButton("Annulla", (dialog2, which2) -> dialog2.cancel());
                    builder.show();
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private boolean existsEntryForDate(String subject, long timestamp) {
        List<StudyEntry> oreList = studyHoursPerSubject.get(subject);
        if (oreList == null) return false;

        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp);
        for (StudyEntry entry : oreList) {
            Calendar cal2 = Calendar.getInstance();
            cal2.setTimeInMillis(entry.timestamp);
            if (cal2.get(Calendar.YEAR) == cal1.get(Calendar.YEAR) &&
                    cal2.get(Calendar.MONTH) == cal1.get(Calendar.MONTH) &&
                    cal2.get(Calendar.DAY_OF_MONTH) == cal1.get(Calendar.DAY_OF_MONTH))
                return true;
        }
        return false;
    }

    private void showEditEntriesDialog() {
        if (selectedSubject == null) return;

        List<StudyEntry> entries = studyHoursPerSubject.get(selectedSubject);
        if (entries == null || entries.isEmpty()) {
            Toast.makeText(requireContext(), "Nessuna entry da modificare", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Modifica/Oppure Elimina - " + selectedSubject);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_edit_entries, null);
        builder.setView(dialogView);

        ListView entriesList = dialogView.findViewById(R.id.entriesListView);

        StudyEntryAdapter adapter = new StudyEntryAdapter(requireContext(), entries);
        entriesList.setAdapter(adapter);

        entriesList.setOnItemClickListener((parent, view, position, id) -> {
            StudyEntry selectedEntry = adapter.getItem(position);
            if (selectedEntry.isLegacy) {
                Toast.makeText(requireContext(), "Dati legacy non modificabili", Toast.LENGTH_SHORT).show();
                return;
            }
            showModifyOrDeleteDialog(selectedSubject, selectedEntry, position, adapter);
        });

        builder.setNegativeButton("Chiudi", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showModifyOrDeleteDialog(String subject, StudyEntry entry, int position, StudyEntryAdapter adapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Modifica Ore - " + dateFormatter.format(new Date(entry.timestamp)));

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(entry.hours));
        builder.setView(input);

        builder.setPositiveButton("Salva", (dialog, which) -> {
            try {
                float newVal = Float.parseFloat(input.getText().toString());
                entry.hours = newVal;
                saveDataToFirebase();
                updateWeeklyChart();
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Valore non valido", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNeutralButton("Elimina", (dialog, which) -> {
            studyHoursPerSubject.get(subject).remove(position);
            saveDataToFirebase();
            updateWeeklyChart();
            adapter.remove(adapter.getItem(position));
            adapter.notifyDataSetChanged();
            Toast.makeText(requireContext(), "Entry eliminata", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void updateWeeklyChart() {
        if (selectedSubject == null) {
            weeklyChart.clear();
            weeklyChart.invalidate();
            return;
        }
        List<StudyEntry> ore = studyHoursPerSubject.get(selectedSubject);
        if (ore == null || ore.isEmpty()) {
            weeklyChart.clear();
            weeklyChart.invalidate();
            return;
        }

        Collections.sort(ore, Comparator.comparingLong(e -> e.timestamp));
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < ore.size(); i++) {
            entries.add(new Entry(i, ore.get(i).hours));
            labels.add(dateFormatter.format(new Date(ore.get(i).timestamp)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Ore di studio: " + selectedSubject);
        dataSet.setColor(Color.RED);
        dataSet.setCircleColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        weeklyChart.setData(lineData);

        XAxis xAxis = weeklyChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(Math.min(labels.size(), 10));
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) return labels.get(index);
                else return "";
            }
        });

        // Fissa centro asse X e Y a 0
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(entries.size() > 0 ? entries.size() - 1 : 0);

        YAxis leftAxis = weeklyChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // minimo Y fissato a 0
        leftAxis.setAxisMaximum(10f); // massimo Y fissato a 10 (modifica se serve)

        weeklyChart.getAxisRight().setEnabled(false);
        weeklyChart.getDescription().setEnabled(false);
        weeklyChart.getLegend().setEnabled(false);

        weeklyChart.invalidate();
    }

    private void saveDataToFirebase() {
        if (user == null) return;
        Map<String, List<Map<String, Object>>> toSave = new HashMap<>();
        for (Map.Entry<String, List<StudyEntry>> entry : studyHoursPerSubject.entrySet()) {
            List<Map<String, Object>> converted = new ArrayList<>();
            for (StudyEntry se : entry.getValue()) {
                Map<String, Object> m = new HashMap<>();
                m.put("timestamp", se.timestamp);
                m.put("hours", se.hours);
                m.put("isLegacy", se.isLegacy);
                converted.add(m);
            }
            toSave.put(entry.getKey(), converted);
        }
        db.collection("users").document(user.getUid())
                .set(toSave)
                .addOnSuccessListener(aVoid -> { })
                .addOnFailureListener(e -> { });
    }

    @SuppressWarnings("unchecked")
    private void loadDataFromFirebase() {
        if (user == null) return;
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> map = documentSnapshot.getData();
                        if (map != null) {
                            studyHoursPerSubject.clear();
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                String subject = entry.getKey();
                                List<?> loadedList = (List<?>) entry.getValue();
                                List<StudyEntry> resultList = new ArrayList<>();
                                for (Object el : loadedList) {
                                    if (el instanceof Map) {
                                        Map<String, Object> elMap = (Map<String, Object>) el;
                                        long t = ((Number) elMap.get("timestamp")).longValue();
                                        float h = ((Number) elMap.get("hours")).floatValue();
                                        boolean isLegacy = false;
                                        if (elMap.get("isLegacy") != null) {
                                            isLegacy = (boolean) elMap.get("isLegacy");
                                        }
                                        StudyEntry entryObj = new StudyEntry(t, h);
                                        entryObj.isLegacy = isLegacy;
                                        resultList.add(entryObj);
                                    } else if (el instanceof Number) {
                                        resultList.add(new StudyEntry(System.currentTimeMillis(), ((Number) el).floatValue()));
                                    }
                                }
                                studyHoursPerSubject.put(subject, resultList);
                            }
                            subjectAdapter.clear();
                            subjectAdapter.addAll(studyHoursPerSubject.keySet());
                            subjectAdapter.notifyDataSetChanged();
                            if (!studyHoursPerSubject.isEmpty()) {
                                selectedSubject = subjectAdapter.getItem(0);
                                subjectSpinner.setSelection(0);
                                updateWeeklyChart();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {});
    }

    private static class StudyEntryAdapter extends ArrayAdapter<StudyEntry> {
        private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        public StudyEntryAdapter(@NonNull Context context, @NonNull List<StudyEntry> objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            StudyEntry entry = getItem(position);
            TextView text = (TextView) v.findViewById(android.R.id.text1);
            if (entry != null) {
                String legacyText = entry.isLegacy ? " (Legacy, non modificabile)" : "";
                text.setText(sdf.format(new Date(entry.timestamp)) + " : " + entry.hours + " ore" + legacyText);
            }
            return v;
        }
    }
}
