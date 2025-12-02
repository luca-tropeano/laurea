package com.example.examapplication;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView examsRecyclerView;
    private ExamsAdapter examsAdapter;
    private List<Exam> examList = new ArrayList<>();
    private TextView emptyListTextView;

    private DatabaseReference examsRef;
    private String userId;

    private static final String DB_URL =
            "https://examapplication-a6835-default-rtdb.europe-west1.firebasedatabase.app/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        examsRecyclerView = rootView.findViewById(R.id.examsRecyclerView);
        examsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        examsAdapter = new ExamsAdapter(examList, this::onExamClicked);
        examsRecyclerView.setAdapter(examsAdapter);

        emptyListTextView = rootView.findViewById(R.id.emptyListTextView);

        Button addExamButton = rootView.findViewById(R.id.addExamButton);
        addExamButton.setOnClickListener(v -> showAddExamDialog());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            if (emptyListTextView != null) {
                emptyListTextView.setText("Utente non autenticato");
                emptyListTextView.setVisibility(View.VISIBLE);
            }
            return rootView;
        }
        userId = user.getUid();

        examsRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("users/users")
                .child(userId)
                .child("exams");

        loadExamsFromDatabase();

        return rootView;
    }

    private void onExamClicked(Exam exam) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(exam.name);
        String[] options = {"Modifica", "Elimina", "Imposta notifica"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    showEditExamDialog(exam);
                    break;
                case 1:
                    deleteExam(exam);
                    break;
                case 2:
                    showNotificationTimeDialog(exam);
                    break;
            }
        });
        builder.show();
    }

    private void showAddExamDialog() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_edit_exam, null);

        EditText examNameInput = dialogView.findViewById(R.id.examNameEditText);
        DatePicker examDatePicker = dialogView.findViewById(R.id.examDatePicker);
        EditText examHourInput = dialogView.findViewById(R.id.examHourEditText);
        EditText examMinuteInput = dialogView.findViewById(R.id.examMinuteEditText);

        DatePicker notificationDatePicker = dialogView.findViewById(R.id.notificationDatePicker);
        EditText notificationHourInput = dialogView.findViewById(R.id.notificationHourEditText);
        EditText notificationMinuteInput = dialogView.findViewById(R.id.notificationMinuteEditText);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Nuovo esame");
        builder.setView(dialogView);
        builder.setPositiveButton("Salva", (dialog, which) -> {
            String name = examNameInput.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Inserisci il nome dell'esame", Toast.LENGTH_SHORT).show();
                return;
            }

            // Leggi DATA/ORA ESAME
            int examDay = examDatePicker.getDayOfMonth();
            int examMonth = examDatePicker.getMonth();
            int examYear = examDatePicker.getYear();
            int examHour, examMinute;
            try {
                examHour = Integer.parseInt(examHourInput.getText().toString());
                if (examHour < 0 || examHour > 23) throw new NumberFormatException();
                examMinute = Integer.parseInt(examMinuteInput.getText().toString());
                if (examMinute < 0 || examMinute > 59) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Orario esame non valido (0-23 ore, 0-59 min)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crea timestamp ESAME
            Calendar examCal = Calendar.getInstance();
            examCal.set(examYear, examMonth, examDay, examHour, examMinute, 0);
            examCal.set(Calendar.MILLISECOND, 0);
            long examDateMillis = examCal.getTimeInMillis();

            // Leggi DATA/ORA NOTIFICA
            int notifDay = notificationDatePicker.getDayOfMonth();
            int notifMonth = notificationDatePicker.getMonth();
            int notifYear = notificationDatePicker.getYear();
            int notifHour, notifMinute;
            try {
                notifHour = Integer.parseInt(notificationHourInput.getText().toString());
                if (notifHour < 0 || notifHour > 23) throw new NumberFormatException();
                notifMinute = Integer.parseInt(notificationMinuteInput.getText().toString());
                if (notifMinute < 0 || notifMinute > 59) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Orario notifica non valido (0-23 ore, 0-59 min)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crea timestamp NOTIFICA
            Calendar notifCal = Calendar.getInstance();
            notifCal.set(notifYear, notifMonth, notifDay, notifHour, notifMinute, 0);
            notifCal.set(Calendar.MILLISECOND, 0);
            long notificationTimeMillis = notifCal.getTimeInMillis();

            if (notifCal.before(Calendar.getInstance())) {
                Toast.makeText(getContext(), "Orario notifica già passato", Toast.LENGTH_SHORT).show();
                return;
            }

            String examId = examsRef.push().getKey();
            if (examId == null) return;

            // Salva SOLO data/ora esame
            Exam exam = new Exam(examId, name, examDateMillis);
            saveExamToDatabase(exam);

            // Imposta notifica
            scheduleExamNotification(requireContext(), exam, notificationTimeMillis);
        });
        builder.setNegativeButton("Annulla", null);
        builder.show();
    }

    private void showEditExamDialog(Exam exam) {
        // Implementazione simile a showAddExamDialog ma con valori preimpostati
        // Per brevità, usa lo stesso dialog ma popola i campi con exam.dateMillis
        showAddExamDialog(); // TODO: implementa versione completa per modifica
    }

    private void deleteExam(Exam exam) {
        examsRef.child(exam.id).removeValue()
                .addOnSuccessListener(aVoid -> {
                    examList.remove(exam);
                    examsAdapter.notifyDataSetChanged();
                    updateEmptyTextVisibility();
                    cancelExamNotification(requireContext(), exam);
                    Toast.makeText(getContext(), "Esame eliminato", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Errore eliminazione: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveExamToDatabase(Exam exam) {
        examsRef.child(exam.id).setValue(exam)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Esame salvato!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Errore salvataggio: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void loadExamsFromDatabase() {
        examsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                examList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Exam exam = child.getValue(Exam.class);
                    if (exam != null) examList.add(exam);
                }
                sortExamsByDate();
                examsAdapter.notifyDataSetChanged();
                updateEmptyTextVisibility();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Errore lettura: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortExamsByDate() {
        Collections.sort(examList, Comparator.comparingLong(e -> e.dateMillis));
    }

    private void updateEmptyTextVisibility() {
        if (emptyListTextView != null) {
            emptyListTextView.setVisibility(examList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void showNotificationTimeDialog(Exam exam) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_set_notification, null);

        DatePicker notificationDatePicker = dialogView.findViewById(R.id.notificationDatePicker);
        EditText notificationHourInput = dialogView.findViewById(R.id.notificationHourEditText);
        EditText notificationMinuteInput = dialogView.findViewById(R.id.notificationMinuteEditText);

        // Imposta data/ora notifica di default a oggi e ora corrente
        Calendar now = Calendar.getInstance();
        notificationDatePicker.updateDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        notificationHourInput.setText(String.format("%02d", now.get(Calendar.HOUR_OF_DAY)));
        notificationMinuteInput.setText(String.format("%02d", now.get(Calendar.MINUTE)));

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Imposta promemoria per " + exam.name);
        builder.setView(dialogView);
        builder.setPositiveButton("Imposta", (dialog, which) -> {
            int notifDay = notificationDatePicker.getDayOfMonth();
            int notifMonth = notificationDatePicker.getMonth();
            int notifYear = notificationDatePicker.getYear();

            int notifHour, notifMinute;
            try {
                notifHour = Integer.parseInt(notificationHourInput.getText().toString());
                if (notifHour < 0 || notifHour > 23) throw new NumberFormatException();
                notifMinute = Integer.parseInt(notificationMinuteInput.getText().toString());
                if (notifMinute < 0 || notifMinute > 59) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Inserisci un orario valido (0-23 ore, 0-59 minuti)", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar notifCal = Calendar.getInstance();
            notifCal.set(notifYear, notifMonth, notifDay, notifHour, notifMinute, 0);
            notifCal.set(Calendar.MILLISECOND, 0);

            if (notifCal.before(Calendar.getInstance())) {
                Toast.makeText(getContext(), "Orario notifica già passato", Toast.LENGTH_SHORT).show();
                return;
            }

            scheduleExamNotification(requireContext(), exam, notifCal.getTimeInMillis());
            Toast.makeText(getContext(), "Promemoria impostato", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Annulla", null);
        builder.show();
    }



    private void scheduleExamNotification(Context context, Exam exam, long notificationTimeMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Calendar notifCal = Calendar.getInstance();
        notifCal.setTimeInMillis(notificationTimeMillis);

        if (notifCal.before(Calendar.getInstance())) {
            Toast.makeText(context, "Orario notifica già passato per " + exam.name, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(context, ExamNotificationReceiver.class);
        intent.putExtra("examName", exam.name);
        intent.putExtra("examId", exam.id);
        intent.putExtra("examDateMillis", exam.dateMillis); // PASSA DATA/ORA ESAME

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                exam.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notifCal.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    Toast.makeText(context,
                            "Permesso per allarmi esatti non concesso. Controlla le impostazioni dell'app.",
                            Toast.LENGTH_LONG).show();
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notifCal.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        notifCal.getTimeInMillis(),
                        pendingIntent
                );
            }

            Toast.makeText(context,
                    "Notifica impostata per " + exam.name + " il " +
                            new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.ITALY)
                                    .format(new java.util.Date(notifCal.getTimeInMillis())),
                    Toast.LENGTH_LONG).show();

        } catch (SecurityException e) {
            Toast.makeText(context,
                    "Impossibile impostare allarme esatto: permesso negato",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Errore impostazione notifica", Toast.LENGTH_SHORT).show();
        }
    }


    private void cancelExamNotification(Context context, Exam exam) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, ExamNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                exam.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
