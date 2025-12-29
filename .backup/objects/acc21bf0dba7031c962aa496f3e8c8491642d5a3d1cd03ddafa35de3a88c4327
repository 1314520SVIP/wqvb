package com.example.textadventure;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView gameOutput;
    private EditText commandInput;
    private Button submitButton;
    private GameEngine gameEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameOutput = findViewById(R.id.game_output);
        commandInput = findViewById(R.id.command_input);
        submitButton = findViewById(R.id.submit_button);

        gameEngine = new GameEngine();
        gameOutput.setText(gameEngine.startGame());

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String command = commandInput.getText().toString().trim();
                String result = gameEngine.processCommand(command);
                gameOutput.setText(gameEngine.getGameState() + "\n\n> " + command + "\n" + result);
                commandInput.setText("");
            }
        });
    }
}
