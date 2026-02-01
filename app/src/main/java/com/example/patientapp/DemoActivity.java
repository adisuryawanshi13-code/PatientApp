package com.example.patientapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.Map;

public class DemoActivity extends AppCompatActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        textView = findViewById(R.id.textView);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("User")   // ⚠️ match EXACT collection name
                .get()
                .addOnSuccessListener(result -> {

                    StringBuilder data = new StringBuilder();

                    for (QueryDocumentSnapshot doc : result) {

                        data.append("====== USER ======\n");

                        // Basic fields
                        data.append("Name: ")
                                .append(doc.getString("firstName")).append(" ")
                                .append(doc.getString("lastName")).append("\n");

                        data.append("Phone: ").append(doc.getString("phone")).append("\n");
                        data.append("Role: ").append(doc.getString("role")).append("\n");
                        data.append("Gender: ").append(doc.getString("gender")).append("\n");
                        data.append("Blood Group: ").append(doc.getString("bloodGroup")).append("\n");
                        data.append("Height: ").append(doc.getLong("heightCm")).append("\n\n");

                        // Emergency Contact (MAP)
                        Map<String, Object> emergency =
                                (Map<String, Object>) doc.get("emergencyContact");

                        if (emergency != null) {
                            data.append("Emergency Contact:\n");
                            data.append("  Name: ").append(emergency.get("name")).append("\n");
                            data.append("  Phone: ").append(emergency.get("phone")).append("\n\n");
                        }

                        // Chronic Conditions (ARRAY)
                        List<String> conditions =
                                (List<String>) doc.get("chronicConditions");

                        if (conditions != null) {
                            data.append("Conditions: ").append(conditions.toString()).append("\n\n");
                        }

                        data.append("----------------------------\n\n");
                    }

                    textView.setText(data.toString());
                })
                .addOnFailureListener(e ->
                        textView.setText("Failed to load data: " + e.getMessage()));
    }
}
