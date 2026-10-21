import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class IfElseGame {
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 100;
    private static final Color BACKGROUND = new Color(10, 15, 30);
    private static final Color CARD = new Color(22, 31, 52);
    private static final Color MUTED = new Color(151, 166, 191);
    private static final Color CYAN = new Color(75, 220, 235);
    private static final Color GREEN = new Color(98, 230, 167);
    private static final Color PINK = new Color(255, 123, 157);
    private static final Color VIOLET = new Color(190, 142, 255);
    private static final Color AMBER = new Color(255, 198, 104);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameWindow game = new GameWindow();
            game.setVisible(true);
            game.enterFullScreen();
        });
    }

    private static class GameWindow extends JFrame {
        private final Random random = new Random();
        private final JTextField guessField = new JTextField(5);
        private final JButton traceButton = new PillButton("Trace my guess  →", true);
        private final JButton hintButton = new PillButton("Logic puzzle", false);
        private final JButton fillButton = new PillButton("Fill the blank", false);
        private final JButton newGameButton = new PillButton("New game", false);
        private final JButton trueButton = new PillButton("TRUE", true);
        private final JButton falseButton = new PillButton("FALSE", false);
        private final JButton thirdChoiceButton = new PillButton("TOO HIGH", false);
        private final JTextField puzzleAnswerField = new JTextField(5);
        private final JButton checkPuzzleButton = new PillButton("Check answer", true);
        private final JLabel statusLabel = new JLabel("Enter a guess to send it through the code.");
        private final JLabel attemptLabel = new JLabel("TRIES  0");
        private final JLabel tutorialLabel = new JLabel("1. Enter a number, then trace the highlighted branch.");
        private final JLabel inputLabel = new JLabel("YOUR GUESS");
        private final CodePanel codePanel = new CodePanel();
        private final FlowPanel flowPanel = new FlowPanel(codePanel);
        private int secretNumber;
        private int attempts;
        private boolean roundWon;
        private int knownMinimum = MIN_NUMBER;
        private int knownMaximum = MAX_NUMBER;
        private boolean hintPending;
        private boolean fillPending;
        private int puzzleGuess;
        private int puzzleTarget;
        private int fillSample;
        private int fillTarget;

        GameWindow() {
            super("Branch Runner — If/Else Game");
            setUndecorated(true);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setMinimumSize(new Dimension(1040, 760));
            setSize(1320, 900);
            setLocationRelativeTo(null);
            getContentPane().setBackground(BACKGROUND);
            setLayout(new BorderLayout(0, 0));

            add(buildHeader(), BorderLayout.NORTH);
            add(buildLearningArea(), BorderLayout.CENTER);
            add(buildStatusBar(), BorderLayout.SOUTH);

            traceButton.addActionListener(event -> submitGuess());
            guessField.addActionListener(event -> submitGuess());
            hintButton.addActionListener(event -> startHintPuzzle());
            fillButton.addActionListener(event -> startFillPuzzle());
            trueButton.addActionListener(event -> answerLogicPuzzle("CORRECT"));
            falseButton.addActionListener(event -> answerLogicPuzzle("TOO LOW"));
            thirdChoiceButton.addActionListener(event -> answerLogicPuzzle("TOO HIGH"));
                checkPuzzleButton.addActionListener(event -> answerFillPuzzle());
                puzzleAnswerField.addActionListener(event -> answerFillPuzzle());
            newGameButton.addActionListener(event -> startNewGame());
                puzzleAnswerField.setFont(new Font("SansSerif", Font.BOLD, 16));
                puzzleAnswerField.setHorizontalAlignment(SwingConstants.CENTER);
                puzzleAnswerField.setForeground(Color.WHITE);
                puzzleAnswerField.setCaretColor(AMBER);
                puzzleAnswerField.setBackground(new Color(13, 21, 39));
                puzzleAnswerField.setEnabled(false);
                puzzleAnswerField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(133, 96, 57), 1, true),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
                codePanel.setPuzzleControls(trueButton, falseButton, thirdChoiceButton,
                    puzzleAnswerField, checkPuzzleButton);
            guessField.setFont(new Font("SansSerif", Font.BOLD, 20));
            guessField.setHorizontalAlignment(SwingConstants.CENTER);
            guessField.setForeground(Color.WHITE);
            guessField.setCaretColor(CYAN);
            guessField.setBackground(new Color(13, 21, 39));
            guessField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(61, 81, 112), 1, true),
                    BorderFactory.createEmptyBorder(9, 12, 9, 12)));

            startNewGame();
        }

        private void enterFullScreen() {
            GraphicsDevice screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (screen.isFullScreenSupported()) {
                screen.setFullScreenWindow(this);
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        }

        private JPanel buildHeader() {
            JPanel header = new JPanel(new BorderLayout(18, 0));
            header.setBackground(BACKGROUND);
            header.setBorder(BorderFactory.createEmptyBorder(22, 28, 18, 28));

            JPanel titleBlock = new JPanel();
            titleBlock.setOpaque(false);
            titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
            JLabel eyebrow = new JLabel("JAVA • CONTROL FLOW LAB");
            eyebrow.setForeground(CYAN);
            eyebrow.setFont(new Font("SansSerif", Font.BOLD, 11));
            JLabel title = new JLabel("Branch Runner");
            title.setForeground(Color.WHITE);
            title.setFont(new Font("SansSerif", Font.BOLD, 29));
            JLabel subtitle = new JLabel("Watch your guess travel through an if / else-if / else statement.");
            subtitle.setForeground(MUTED);
            subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
            titleBlock.add(eyebrow);
            titleBlock.add(Box.createVerticalStrut(4));
            titleBlock.add(title);
            titleBlock.add(Box.createVerticalStrut(3));
            titleBlock.add(subtitle);

            header.add(titleBlock, BorderLayout.CENTER);
            return header;
        }

        private JPanel buildLearningArea() {
            JPanel area = new JPanel(new BorderLayout(14, 0));
            area.setBackground(BACKGROUND);
            area.setBorder(BorderFactory.createEmptyBorder(0, 28, 14, 28));

                JPanel codeCard = new SoftPanel(CARD, 28);
                codeCard.setLayout(new BorderLayout(0, 14));
                codeCard.setBorder(BorderFactory.createEmptyBorder(19, 17, 17, 17));
            JLabel codeTitle = new JLabel("LIVE CODE");
            codeTitle.setForeground(VIOLET);
            codeTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
            JPanel codeHeading = new JPanel(new BorderLayout(0, 7));
            codeHeading.setOpaque(false);
            JLabel legend = new JLabel("AMBER  question   •   VIOLET  target values");
            legend.setForeground(new Color(172, 185, 210));
            legend.setFont(new Font("SansSerif", Font.BOLD, 12));
            codeHeading.add(codeTitle, BorderLayout.NORTH);
            codeHeading.add(legend, BorderLayout.SOUTH);
            codeCard.add(codeHeading, BorderLayout.NORTH);
            codeCard.add(codePanel, BorderLayout.CENTER);
            codeCard.setPreferredSize(new Dimension(455, 590));

                JPanel flowCard = new SoftPanel(CARD, 28);
                flowCard.setLayout(new BorderLayout());
                flowCard.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            flowCard.add(flowPanel, BorderLayout.CENTER);

            area.add(codeCard, BorderLayout.WEST);
            area.add(flowCard, BorderLayout.CENTER);
            return area;
        }

        private JPanel buildStatusBar() {
            JPanel dock = new JPanel(new BorderLayout(0, 10));
            dock.setOpaque(false);
            dock.setBorder(BorderFactory.createEmptyBorder(0, 28, 20, 28));

            JPanel status = new SoftPanel(new Color(26, 31, 63), 22);
            status.setLayout(new BorderLayout(15, 0));
            status.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
            statusLabel.setForeground(Color.WHITE);
            statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            attemptLabel.setForeground(CYAN);
            attemptLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            status.add(statusLabel, BorderLayout.CENTER);
            JPanel guideAndCount = new JPanel();
            guideAndCount.setOpaque(false);
            guideAndCount.setLayout(new BoxLayout(guideAndCount, BoxLayout.Y_AXIS));
            tutorialLabel.setForeground(AMBER);
            tutorialLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            attemptLabel.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
            guideAndCount.add(tutorialLabel);
            guideAndCount.add(Box.createVerticalStrut(4));
            guideAndCount.add(attemptLabel);
            status.add(guideAndCount, BorderLayout.EAST);

            JPanel actions = new SoftPanel(new Color(30, 39, 72), 28);
            actions.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
            actions.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
            inputLabel.setForeground(MUTED);
            inputLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            guessField.setPreferredSize(new Dimension(82, 42));
            actions.add(inputLabel);
            actions.add(guessField);
            actions.add(traceButton);
            trueButton.setVisible(false);
            falseButton.setVisible(false);
            thirdChoiceButton.setVisible(false);
            JPanel hintsSection = new SoftPanel(new Color(42, 35, 72), 22);
            hintsSection.setLayout(new BorderLayout(0, 3));
            hintsSection.setBorder(BorderFactory.createEmptyBorder(5, 10, 7, 10));
            JLabel hintsTitle = new JLabel("HINTS", SwingConstants.CENTER);
            hintsTitle.setForeground(VIOLET);
            hintsTitle.setFont(new Font("SansSerif", Font.BOLD, 10));
            JPanel hintButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            hintButtons.setOpaque(false);
            hintButtons.add(hintButton);
            hintButtons.add(fillButton);
            hintsSection.add(hintsTitle, BorderLayout.NORTH);
            hintsSection.add(hintButtons, BorderLayout.CENTER);
            actions.add(hintsSection);
            actions.add(newGameButton);

            dock.add(status, BorderLayout.NORTH);
            dock.add(actions, BorderLayout.CENTER);
            return dock;
        }

        private void submitGuess() {
            if (flowPanel.isAnimating() || roundWon) {
                return;
            }
            if (hintPending) {
                statusLabel.setText("Choose the output branch shown by the logic puzzle.");
                return;
            }
            if (fillPending) {
                answerFillPuzzle();
                return;
            }
            int guess;
            try {
                guess = Integer.parseInt(guessField.getText().trim());
            } catch (NumberFormatException exception) {
                statusLabel.setText("Type a whole number from 1 to 100 first.");
                tutorialLabel.setText("Enter digits only, then press Trace my guess.");
                return;
            }
            if (guess < MIN_NUMBER || guess > MAX_NUMBER) {
                statusLabel.setText("Keep your guess between 1 and 100.");
                tutorialLabel.setText("Choose a number inside the 1–100 range.");
                return;
            }

            attempts++;
            attemptLabel.setText("TRIES  " + attempts);
            guessField.setEnabled(false);
            traceButton.setEnabled(false);

            List<String> route = new ArrayList<>();
            List<Integer> lines = new ArrayList<>();
            route.add("start");
            route.add("if");
            lines.add(0);

            boolean correct;
            if (guess == secretNumber) {
                route.add("correct");
                lines.add(1);
                correct = true;
            } else if (guess < secretNumber) {
                route.add("elseIf");
                lines.add(2);
                route.add("low");
                lines.add(3);
                correct = false;
            } else {
                route.add("elseIf");
                lines.add(2);
                route.add("high");
                lines.add(5);
                correct = false;
            }

            roundWon = correct;
            flowPanel.animateGuess(guess, secretNumber, route, lines, () -> {
                hintButton.setEnabled(!roundWon);
                fillButton.setEnabled(!roundWon);
                if (roundWon) {
                    knownMinimum = secretNumber;
                    knownMaximum = secretNumber;
                    flowPanel.setKnownRange(knownMinimum, knownMaximum);
                    statusLabel.setText("Correct! The if condition was true, so the else branches were skipped.");
                    tutorialLabel.setText("Solved! The first condition was true, so Java skipped the other branches.");
                    traceButton.setText("Round complete");
                    traceButton.setEnabled(false);
                } else if (guess < secretNumber) {
                    knownMinimum = Math.max(knownMinimum, guess + 1);
                    flowPanel.setKnownRange(knownMinimum, knownMaximum);
                    statusLabel.setText("Too low — else-if ran. Possible number: "
                            + knownMinimum + "–" + knownMaximum + ".");
                        tutorialLabel.setText("Too low: guess < secret is true. Try a larger number next.");
                    guessField.setEnabled(true);
                    traceButton.setEnabled(true);
                    guessField.requestFocusInWindow();
                } else {
                    knownMaximum = Math.min(knownMaximum, guess - 1);
                    flowPanel.setKnownRange(knownMinimum, knownMaximum);
                    statusLabel.setText("Too high — else ran. Possible number: "
                            + knownMinimum + "–" + knownMaximum + ".");
                        tutorialLabel.setText("Too high: both tests were false, so the final else ran. Try lower.");
                    guessField.setEnabled(true);
                    traceButton.setEnabled(true);
                    guessField.requestFocusInWindow();
                }
            });
            hintButton.setEnabled(false);
            fillButton.setEnabled(false);
        }

        private void startHintPuzzle() {
            if (flowPanel.isAnimating() || roundWon || hintPending || fillPending) {
                return;
            }
            if (knownMinimum == knownMaximum) {
                statusLabel.setText("The clues already narrow it to " + knownMinimum + "! Enter that guess.");
                tutorialLabel.setText("The range has one number left. Enter it to finish the game.");
                return;
            }
            puzzleGuess = random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
            puzzleTarget = random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
            hintPending = true;
            hintButton.setEnabled(false);
            fillButton.setEnabled(false);
            guessField.setEnabled(false);
            traceButton.setEnabled(false);
            trueButton.setVisible(true);
            falseButton.setVisible(true);
            thirdChoiceButton.setVisible(true);
            trueButton.setText("CORRECT");
            falseButton.setText("TOO LOW");
            thirdChoiceButton.setText("TOO HIGH");
            statusLabel.setText("LOGIC PUZZLE: use the shown guess and target values to trace the code.");
            tutorialLabel.setText("2. Trace from top to bottom; equality is tested before less-than.");
            codePanel.showLogicPuzzle(puzzleGuess, puzzleTarget);
            codePanel.showLogicOptions();
            revalidate();
            repaint();
        }

        private void answerLogicPuzzle(String selectedOutput) {
            if (!hintPending) {
                return;
            }
            String actualOutput = getPracticeOutput(puzzleGuess, puzzleTarget);
            if (!selectedOutput.equals(actualOutput)) {
                statusLabel.setText("Not quite. Follow the checks in order: equality first, then smaller-than.");
                tutorialLabel.setText("Read each condition in order. If one is true, later branches are skipped.");
                codePanel.setPuzzleNote("Not quite. Check equality first, then compare guess &lt; target. The visible values are still here to help.");
                return;
            }
            hintPending = false;
            hidePuzzleChoices();
            statusLabel.setText("Correct! With guess = " + puzzleGuess + " and target = " + puzzleTarget
                    + ", the code prints " + actualOutput + ". This practice example does not change the game target.");
                tutorialLabel.setText("Nice tracing! Start a new puzzle or return to guessing the hidden number.");
            codePanel.showLogicResult(puzzleGuess, puzzleTarget, actualOutput);
            revalidate();
            repaint();
        }

        private void startFillPuzzle() {
            if (flowPanel.isAnimating() || roundWon || hintPending || fillPending) {
                return;
            }
            fillPending = true;
            hintButton.setEnabled(false);
            fillButton.setEnabled(false);
            guessField.setEnabled(false);
            puzzleAnswerField.setEnabled(true);
            puzzleAnswerField.setText("");
            traceButton.setEnabled(false);
            fillSample = random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
            fillTarget = random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
            statusLabel.setText("FILL THE BLANK: use the shown values and enter the greater-than operator.");
            tutorialLabel.setText("2. Compare the two visible values. Use > for greater-than.");
            codePanel.showFillPuzzle(fillSample, fillTarget);
            codePanel.showFillOptions();
            puzzleAnswerField.requestFocusInWindow();
        }

        private void answerFillPuzzle() {
            String operator = puzzleAnswerField.getText().trim();
            if (!operator.equals(">")) {
                statusLabel.setText("Try again: which comparison operator means the left value is greater than the right?");
                tutorialLabel.setText("Hint: > means left is greater; < means left is smaller.");
                return;
            }

            fillPending = false;
            String result = fillSample > fillTarget ? "TRUE" : "FALSE";
            statusLabel.setText("Correct! " + fillSample + " > " + fillTarget + " is " + result
                    + ". This practice example does not change the game target.");
                tutorialLabel.setText("Operator works! The highlighted code shows the resulting branch.");
            codePanel.showFillResult(fillSample, fillTarget, result);
            codePanel.hidePuzzleOptions();
            puzzleAnswerField.setText("");
            puzzleAnswerField.setEnabled(false);
            guessField.setEnabled(true);
            inputLabel.setText("YOUR GUESS");
            traceButton.setText("Trace my guess  →");
            traceButton.setEnabled(true);
            hintButton.setEnabled(true);
            fillButton.setEnabled(true);
            guessField.requestFocusInWindow();
            revalidate();
            repaint();
        }

        private String getPracticeOutput(int guess, int target) {
            if (guess == target) return "CORRECT";
            return guess < target ? "TOO LOW" : "TOO HIGH";
        }

        private void hidePuzzleChoices() {
            trueButton.setVisible(false);
            falseButton.setVisible(false);
            thirdChoiceButton.setVisible(false);
            codePanel.hidePuzzleOptions();
            trueButton.setText("TRUE");
            falseButton.setText("FALSE");
            thirdChoiceButton.setText("TOO HIGH");
            guessField.setEnabled(true);
            traceButton.setText("Trace my guess  →");
            traceButton.setEnabled(true);
            hintButton.setEnabled(true);
            fillButton.setEnabled(true);
            inputLabel.setText("YOUR GUESS");
            guessField.requestFocusInWindow();
        }

        private void startNewGame() {
            secretNumber = random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
            attempts = 0;
            roundWon = false;
            knownMinimum = MIN_NUMBER;
            knownMaximum = MAX_NUMBER;
            hintPending = false;
            fillPending = false;
            attemptLabel.setText("TRIES  0");
            statusLabel.setText("Secret number: 1–100. Guess it or solve a code puzzle to narrow the range.");
            tutorialLabel.setText("1. Enter a number from 1–100. Watch the code check each condition.");
            guessField.setText("");
            puzzleAnswerField.setText("");
            guessField.setEnabled(true);
            traceButton.setText("Trace my guess  →");
            traceButton.setEnabled(true);
            hintButton.setEnabled(true);
            fillButton.setEnabled(true);
            trueButton.setVisible(false);
            falseButton.setVisible(false);
            thirdChoiceButton.setVisible(false);
            codePanel.hidePuzzleOptions();
            puzzleAnswerField.setText("");
            puzzleAnswerField.setEnabled(false);
            inputLabel.setText("YOUR GUESS");
            traceButton.setText("Trace my guess  →");
            flowPanel.reset();
            flowPanel.setKnownRange(knownMinimum, knownMaximum);
            codePanel.resetView("Follow the glowing marker as Java checks each branch.");
            revalidate();
            repaint();
            guessField.requestFocusInWindow();
        }
    }

    private static class SoftPanel extends JPanel {
        private final Color base;
        private final int arc;

        SoftPanel(Color base, int arc) {
            this.base = base;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(2, 7, 18, 60));
            g.fillRoundRect(1, 4, getWidth() - 2, getHeight() - 3, arc, arc);
            g.setPaint(new GradientPaint(0, 0, base.brighter(), getWidth(), getHeight(), base));
            g.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 4, arc, arc);
            g.setColor(new Color(120, 153, 192, 35));
            g.setStroke(new BasicStroke(1f));
            g.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 4, arc, arc);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class PillButton extends JButton {
        private final boolean primary;

        PillButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setForeground(primary ? new Color(7, 22, 34) : Color.WHITE);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(12, 17, 12, 17));
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color top;
            Color bottom;
            if (!isEnabled()) {
                top = new Color(63, 75, 94);
                bottom = new Color(44, 56, 75);
            } else if (getModel().isPressed()) {
                top = primary ? new Color(52, 180, 195) : new Color(59, 77, 108);
                bottom = primary ? new Color(38, 153, 175) : new Color(40, 57, 84);
            } else if (getModel().isRollover()) {
                top = primary ? new Color(133, 247, 250) : new Color(73, 94, 128);
                bottom = primary ? new Color(82, 213, 224) : new Color(47, 67, 98);
            } else {
                top = primary ? new Color(112, 238, 242) : new Color(54, 72, 104);
                bottom = primary ? new Color(64, 199, 215) : new Color(36, 52, 80);
            }
            g.setColor(new Color(2, 7, 18, 70));
            g.fillRoundRect(1, 4, getWidth() - 3, getHeight() - 4, 28, 28);
            g.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 5, 28, 28);
            if (!primary) {
                g.setColor(new Color(163, 190, 225, 70));
                g.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 5, 28, 28);
            }
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class CodePanel extends JPanel {
        private final List<JLabel> lines = new ArrayList<>();
        private final JPanel puzzleControls = new JPanel(new BorderLayout(0, 8));
        private final JPanel logicChoices = new JPanel(new GridBagLayout());
        private final JPanel fillControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        private Timer markerTimer;
        private int markerLine = -1;
        private final String[] code = {
            "if (guess == secret) {",
            "    show(\"Correct!\");",
            "} else if (guess < secret) {",
            "    show(\"Too low!\");",
            "} else {",
            "    show(\"Too high!\");",
            "}"
        };
        private final JLabel explanation = new JLabel(" ");

        CodePanel() {
            setOpaque(false);
            setLayout(new BorderLayout(0, 15));
            JPanel codeLines = new JPanel();
            codeLines.setOpaque(false);
            codeLines.setLayout(new BoxLayout(codeLines, BoxLayout.Y_AXIS));
            for (String sourceLine : code) {
                JLabel line = new JLabel("  " + sourceLine);
                line.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
                line.setForeground(new Color(190, 203, 224));
                line.setBorder(BorderFactory.createEmptyBorder(10, 9, 10, 5));
                line.setOpaque(true);
                line.setBackground(new Color(0, 0, 0, 0));
                lines.add(line);
                codeLines.add(line);
            }
            explanation.setForeground(MUTED);
            explanation.setFont(new Font("SansSerif", Font.PLAIN, 12));
            explanation.setVerticalAlignment(SwingConstants.TOP);
            explanation.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(48, 66, 94), 1, true),
                    BorderFactory.createEmptyBorder(11, 10, 11, 10)));
            explanation.setOpaque(true);
            explanation.setBackground(new Color(15, 24, 42));
            explanation.setFont(new Font("SansSerif", Font.PLAIN, 14));
            explanation.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(69, 79, 115), 1, true),
                    BorderFactory.createEmptyBorder(14, 14, 14, 14)));
            puzzleControls.setOpaque(false);
            logicChoices.setOpaque(false);
            fillControls.setOpaque(false);
            logicChoices.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            puzzleControls.add(logicChoices, BorderLayout.CENTER);
            puzzleControls.setVisible(false);
            add(codeLines, BorderLayout.NORTH);
            add(explanation, BorderLayout.CENTER);
            add(puzzleControls, BorderLayout.SOUTH);
        }

        void setPuzzleControls(JButton correct, JButton low, JButton high,
                JTextField answerField, JButton checkButton) {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.insets = new Insets(4, 6, 4, 6);
            constraints.gridx = 0;
            constraints.gridwidth = 2;
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.gridy = 0;
            logicChoices.add(correct, constraints);
            constraints.gridwidth = 1;
            constraints.gridx = 0;
            constraints.gridy = 1;
            logicChoices.add(low, constraints);
            constraints.gridx = 1;
            constraints.gridy = 1;
            logicChoices.add(high, constraints);
            answerField.setPreferredSize(new Dimension(92, 40));
            fillControls.add(answerField);
            fillControls.add(checkButton);
            fillControls.setVisible(false);
            puzzleControls.add(fillControls, BorderLayout.SOUTH);
        }

        void showLogicOptions() {
            logicChoices.setVisible(true);
            fillControls.setVisible(false);
            puzzleControls.setVisible(true);
            revalidate();
            repaint();
        }

        void showFillOptions() {
            logicChoices.setVisible(false);
            fillControls.setVisible(true);
            puzzleControls.setVisible(true);
            revalidate();
            repaint();
        }

        void hidePuzzleOptions() {
            logicChoices.setVisible(false);
            fillControls.setVisible(false);
            puzzleControls.setVisible(false);
            revalidate();
            repaint();
        }

        void setTrace(int activeLine, String note) {
            explanation.setText("<html><div style='width:340px'>" + note + "</div></html>");
            if (markerTimer != null) {
                markerTimer.stop();
            }
            if (activeLine < 0) {
                markerLine = -1;
                paintMarker();
                return;
            }
            if (markerLine < 0) {
                markerLine = activeLine;
                paintMarker();
                return;
            }
            final int destination = activeLine;
            markerTimer = new Timer(52, event -> {
                if (markerLine == destination) {
                    markerTimer.stop();
                    return;
                }
                markerLine += markerLine < destination ? 1 : -1;
                paintMarker();
            });
            markerTimer.start();
        }

        void resetView(String note) {
            if (markerTimer != null) {
                markerTimer.stop();
            }
            markerLine = -1;
            for (int i = 0; i < lines.size(); i++) {
                lines.get(i).setText("  " + code[i]);
                lines.get(i).setBackground(new Color(0, 0, 0, 0));
                lines.get(i).setForeground(new Color(190, 203, 224));
                lines.get(i).setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
            }
            explanation.setText("<html><div style='width:340px'>" + note + "</div></html>");
            revalidate();
            repaint();
        }

        void showLogicPuzzle(int sampleGuess, int target) {
            if (markerTimer != null) {
                markerTimer.stop();
            }
            markerLine = 0;
            String[] puzzleCode = {
                "int guess = " + sampleGuess + ";",
                "int target = " + target + ";",
                "if (guess == target) {",
                "    print(\"CORRECT\");",
                "} else if (guess < target) {",
                "    print(\"TOO LOW\");",
                "} else { print(\"TOO HIGH\"); }"
            };
                showPuzzleCode(puzzleCode, "Both values are visible. Trace equality first, then <code>guess &lt; target</code>."
                    + "<br><br>Choose the output: CORRECT, TOO LOW, or TOO HIGH.",
                    new int[] { 2, 4 }, new int[] { 0, 1 });
        }

        void showLogicResult(int guess, int target, String output) {
            String[] resultCode = {
                "int guess = " + guess + ";",
                "int target = " + target + ";",
                "if (guess == target) {",
                "    print(\"CORRECT\");",
                "} else if (guess < target) {",
                "    print(\"TOO LOW\");",
                "} else { print(\"TOO HIGH\"); }"
            };
            showPuzzleCode(resultCode, "Worked trace: with guess = <b>" + guess + "</b> and target = <b>"
                    + target + "</b>, Java takes the <b>" + output
                    + "</b> path. The hidden game target is unchanged.",
                    new int[] { output.equals("CORRECT") ? 2 : output.equals("TOO LOW") ? 4 : 6 },
                    new int[] { 0, 1 });
        }

        void setPuzzleNote(String note) {
            explanation.setText("<html><div style='width:300px'>" + note + "</div></html>");
            explanation.revalidate();
            explanation.repaint();
        }

        void showFillPuzzle(int sample, int target) {
            String[] puzzleCode = {
                "int sample = " + sample + ";",
                "int target = " + target + ";",
                "if (sample ___ target) {",
                "    print(\"ABOVE\");",
                "} else { print(\"NOT ABOVE\"); }"
            };
                showPuzzleCode(puzzleCode, "Both values are visible. Fill the blank with the operator that means <b>sample is greater than target</b>."
                    + "<br><br>Type the operator in the answer box, then select Check code.",
                    new int[] { 2 }, new int[] { 0, 1 });
        }

        void showFillResult(int sample, int target, String result) {
            String[] resultCode = {
                "int sample = " + sample + ";",
                "int target = " + target + ";",
                "if (sample > target) {",
                "    print(\"ABOVE\");",
                "} else { print(\"NOT ABOVE\"); }"
            };
            showPuzzleCode(resultCode, "Correct operator: <b>&gt;</b>. Since " + sample + " &gt; "
                    + target + " is " + result + ", this practice code prints "
                    + (result.equals("TRUE") ? "ABOVE" : "NOT ABOVE")
                    + ". The hidden game target is not involved.",
                    new int[] { 2 }, new int[] { 0, 1 });
        }

        private void showPuzzleCode(String[] puzzleCode, String note) {
            showPuzzleCode(puzzleCode, note, new int[0], new int[0]);
        }

        private void showPuzzleCode(String[] puzzleCode, String note, int[] questionLines, int[] targetLines) {
            for (int i = 0; i < lines.size(); i++) {
                boolean question = contains(questionLines, i);
                boolean target = contains(targetLines, i);
                lines.get(i).setText(i < puzzleCode.length ? (question ? "› " : "  ") + puzzleCode[i] : " ");
                lines.get(i).setBackground(question ? new Color(87, 62, 31)
                        : target ? new Color(62, 43, 94) : new Color(0, 0, 0, 0));
                lines.get(i).setForeground(question ? AMBER : target ? VIOLET : new Color(174, 190, 215));
                lines.get(i).setFont(new Font(Font.MONOSPACED, Font.BOLD, question || target ? 15 : 14));
            }
            explanation.setText("<html><div style='width:340px'>" + note + "</div></html>");
            revalidate();
            repaint();
        }

        private boolean contains(int[] values, int target) {
            for (int value : values) {
                if (value == target) return true;
            }
            return false;
        }

        private void paintMarker() {
            for (int i = 0; i < lines.size(); i++) {
                boolean active = i == markerLine;
                lines.get(i).setBackground(active ? new Color(31, 76, 88) : new Color(0, 0, 0, 0));
                lines.get(i).setForeground(active ? Color.WHITE : new Color(174, 190, 215));
                lines.get(i).setFont(new Font(Font.MONOSPACED, Font.BOLD, active ? 16 : 15));
                lines.get(i).setText((active ? "› " : "  ") + code[i]);
            }
        }
    }

    private static class FlowPanel extends JPanel {
        private final Set<String> completedEdges = new HashSet<>();
        private List<String> route = new ArrayList<>();
        private List<Integer> codeLines = new ArrayList<>();
        private int guess;
        private int secret;
        private int legIndex;
        private float progress;
        private int activeCodeLine = -1;
        private boolean correct;
        private Boolean firstCondition;
        private Boolean secondCondition;
        private String activeNode = "start";
        private Timer timer;
        private Runnable completion;
        private final CodePanel codePanel;
        private int knownMinimum = MIN_NUMBER;
        private int knownMaximum = MAX_NUMBER;

        FlowPanel(CodePanel codePanel) {
            this.codePanel = codePanel;
            setOpaque(false);
            setPreferredSize(new Dimension(700, 570));
        }

        void reset() {
            if (timer != null) {
                timer.stop();
            }
            completedEdges.clear();
            route.clear();
            codeLines.clear();
            activeCodeLine = -1;
            activeNode = "start";
            firstCondition = null;
            secondCondition = null;
            progress = 0f;
            repaint();
        }

        boolean isAnimating() {
            return timer != null && timer.isRunning();
        }

        void setKnownRange(int minimum, int maximum) {
            knownMinimum = minimum;
            knownMaximum = maximum;
            repaint();
        }

        void animateGuess(int guess, int secret, List<String> route, List<Integer> lines, Runnable completion) {
            reset();
            this.guess = guess;
            this.secret = secret;
            this.correct = guess == secret;
            this.firstCondition = null;
            this.secondCondition = null;
            this.route = route;
            this.codeLines = lines;
            this.completion = completion;
            legIndex = 0;
            progress = 0f;
            advanceLeg();
            timer = new Timer(32, event -> {
                progress += 0.045f;
                if (progress >= 1f) {
                    progress = 1f;
                    String edge = edgeKey(route.get(legIndex), route.get(legIndex + 1));
                    completedEdges.add(edge);
                    activeNode = route.get(legIndex + 1);
                    if (activeNode.equals("if")) {
                        firstCondition = guess == secret;
                    } else if (activeNode.equals("elseIf")) {
                        secondCondition = guess < secret;
                    }
                    legIndex++;
                    if (legIndex >= route.size() - 1) {
                        timer.stop();
                        activeNode = route.get(route.size() - 1);
                        repaint();
                        completion.run();
                    } else {
                        progress = 0f;
                        advanceLeg();
                    }
                }
                repaint();
            });
            timer.start();
        }

        private void advanceLeg() {
            activeCodeLine = codeLines.get(legIndex);
            String note;
            if (activeCodeLine == 0) {
                note = "The guess enters the if statement. Java checks this condition first.";
            } else if (activeCodeLine == 1) {
                note = "TRUE — the exact-match branch runs. The remaining branches are skipped.";
            } else if (activeCodeLine == 2) {
                note = "FALSE — Java continues to the else-if condition because the first test failed.";
            } else if (activeCodeLine == 3) {
                note = "TRUE — the guess is smaller, so the else-if branch runs.";
            } else {
                note = "Both tests are false, so Java falls through to the final else branch.";
            }
            if (codePanel != null) {
                codePanel.setTrace(activeCodeLine, note);
            }
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
                g.setPaint(new GradientPaint(0, 0, new Color(31, 34, 71), w, h,
                    new Color(14, 35, 58)));
            g.fillRoundRect(0, 0, w, h, 18, 18);

            drawHeading(g);
            drawConnectors(g, w, h);
            drawNodes(g, w, h);
            drawMovingToken(g, w, h);
            g.dispose();
        }

        private void drawHeading(Graphics2D g) {
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.setColor(new Color(193, 207, 232));
            g.drawString("EXECUTION MAP", 20, 25);

            String range = "PROVEN RANGE  " + knownMinimum + "–" + knownMaximum;
            String rangeText = route.isEmpty() ? range : range + "   •   GUESS " + guess;
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            int pillWidth = g.getFontMetrics().stringWidth(rangeText) + 24;
            int pillX = getWidth() - pillWidth - 16;
            g.setColor(new Color(51, 43, 85));
            g.fillRoundRect(pillX, 7, pillWidth, 28, 18, 18);
            g.setColor(new Color(143, 112, 215));
            g.setStroke(new BasicStroke(1.2f));
            g.drawRoundRect(pillX, 7, pillWidth, 28, 18, 18);
            g.setColor(new Color(226, 213, 255));
            g.drawString(rangeText, pillX + 12, 26);
        }

        private void drawConnectors(Graphics2D g, int w, int h) {
            drawEdge(g, "start", "if", "", w, h);
            drawEdge(g, "if", "correct", "TRUE", w, h);
            drawEdge(g, "if", "elseIf", "FALSE", w, h);
            drawEdge(g, "elseIf", "low", "TRUE", w, h);
            drawEdge(g, "elseIf", "high", "FALSE", w, h);
        }

        private void drawEdge(Graphics2D g, String from, String to, String label, int w, int h) {
            Point start = nodeCenter(from, w, h);
            Point end = nodeCenter(to, w, h);
            Point[] edge = edgePoints(from, to, start, end);
            String key = edgeKey(from, to);
            boolean active = !route.isEmpty() && legIndex < route.size() - 1
                    && key.equals(edgeKey(route.get(legIndex), route.get(legIndex + 1)));
            boolean done = completedEdges.contains(key);
            Color color = active ? CYAN : done ? GREEN : new Color(60, 77, 107);
            g.setColor(color);
            g.setStroke(new BasicStroke(active ? 3.2f : done ? 2.7f : 1.5f,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(edge[0].x, edge[0].y, edge[1].x, edge[1].y);
            drawArrowHead(g, edge[0], edge[1], color);
            if (!label.isEmpty()) {
                int midX = (edge[0].x + edge[1].x) / 2;
                int midY = (edge[0].y + edge[1].y) / 2;
                int offsetX = to.equals("correct") || to.equals("low") ? -25 : 13;
                int offsetY = to.equals("elseIf") ? -11 : -7;
                drawPill(g, midX + offsetX, midY + offsetY, label,
                        active || done ? color : new Color(111, 127, 153));
            }
        }

        private void drawNodes(Graphics2D g, int w, int h) {
            drawStartNode(g, nodeCenter("start", w, h));
            drawDecisionNode(g, "if", nodeCenter("if", w, h), "guess ==", "secret?",
                    firstCondition == null ? "FIRST CHECK" : firstCondition ? "TRUE" : "FALSE",
                    firstCondition != null && activeNode.equals("if"));
                String secondStatus = Boolean.TRUE.equals(firstCondition) ? "NOT CHECKED" : secondCondition == null ? "SECOND CHECK"
                    : secondCondition ? "TRUE" : "FALSE";
                drawDecisionNode(g, "elseIf", nodeCenter("elseIf", w, h), "guess <", "secret?",
                    secondStatus, !Boolean.TRUE.equals(firstCondition) && activeNode.equals("elseIf"));
            drawOutputNode(g, "correct", nodeCenter("correct", w, h), "CORRECT!", "if branch",
                    Boolean.TRUE.equals(firstCondition) && activeNode.equals("correct"), GREEN);
            drawOutputNode(g, "low", nodeCenter("low", w, h), "TOO LOW", "else-if branch",
                    Boolean.FALSE.equals(firstCondition) && Boolean.TRUE.equals(secondCondition)
                        && activeNode.equals("low"), CYAN);
            drawOutputNode(g, "high", nodeCenter("high", w, h), "TOO HIGH", "else branch",
                    Boolean.FALSE.equals(firstCondition) && Boolean.FALSE.equals(secondCondition)
                        && activeNode.equals("high"), PINK);
        }

        private void drawStartNode(Graphics2D g, Point p) {
            int width = 142;
            int height = 49;
            boolean active = activeNode.equals("start") && route.isEmpty();
            g.setColor(new Color(37, 51, 77));
            g.fillRoundRect(p.x - width / 2, p.y - height / 2, width, height, 24, 24);
            g.setColor(new Color(74, 97, 132));
            g.setStroke(new BasicStroke(1.3f));
            g.drawRoundRect(p.x - width / 2, p.y - height / 2, width, height, 24, 24);
            centerText(g, "GUESS INPUT", p.x, p.y + 5, Color.WHITE,
                    new Font("SansSerif", Font.BOLD, 12));
        }

        private void drawDecisionNode(Graphics2D g, String id, Point p, String firstLine,
                String secondLine, String status, boolean active) {
            int width = id.equals("elseIf") ? 202 : 184;
            int height = 82;
            int left = p.x - width / 2;
            int top = p.y - height / 2;
            g.setColor(new Color(4, 10, 21, 90));
            g.fillRoundRect(left + 2, top + 6, width, height, 25, 25);
            g.setPaint(new GradientPaint(left, top, active ? new Color(35, 94, 105)
                : new Color(39, 57, 84), left + width, top + height,
                active ? new Color(24, 61, 79) : new Color(27, 42, 67)));
            g.fillRoundRect(left, top, width, height, 25, 25);
            g.setColor(active ? CYAN : new Color(78, 105, 143));
            g.setStroke(new BasicStroke(active ? 2.4f : 1.3f));
            g.drawRoundRect(left, top, width, height, 25, 25);
            centerText(g, firstLine, p.x, p.y - 7, Color.WHITE,
                    new Font(Font.MONOSPACED, Font.BOLD, 13));
            centerText(g, secondLine, p.x, p.y + 10, Color.WHITE,
                    new Font(Font.MONOSPACED, Font.BOLD, 13));
            centerText(g, status, p.x, p.y + 31,
                status.equals("TRUE") ? GREEN : status.equals("FALSE") ? PINK
                    : status.equals("NOT CHECKED") ? new Color(255, 211, 125) : MUTED,
                    new Font("SansSerif", Font.BOLD, 9));
        }

        private void drawOutputNode(Graphics2D g, String id, Point p, String title, String detail,
                boolean active, Color accent) {
            int width = 156;
            int height = 63;
                int left = p.x - width / 2;
                int top = p.y - height / 2;
                g.setColor(new Color(4, 10, 21, 85));
                g.fillRoundRect(left + 2, top + 5, width, height, 19, 19);
                Color base = active ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 65)
                    : new Color(34, 47, 70);
                g.setPaint(new GradientPaint(left, top, base, left + width, top + height,
                    new Color(23, 36, 58)));
                g.fillRoundRect(left, top, width, height, 19, 19);
            g.setColor(active ? accent : new Color(72, 91, 122));
            g.setStroke(new BasicStroke(active ? 2.5f : 1.2f));
                g.drawRoundRect(left, top, width, height, 19, 19);
            centerText(g, title, p.x, p.y - 3, active ? accent : Color.WHITE,
                    new Font("SansSerif", Font.BOLD, 13));
            centerText(g, detail, p.x, p.y + 15, MUTED,
                    new Font("SansSerif", Font.PLAIN, 10));
        }

        private void drawMovingToken(Graphics2D g, int w, int h) {
            if (route.isEmpty() || legIndex >= route.size() - 1) {
                return;
            }
            Point from = nodeCenter(route.get(legIndex), w, h);
            Point to = nodeCenter(route.get(legIndex + 1), w, h);
            float x = from.x + (to.x - from.x) * progress;
            float y = from.y + (to.y - from.y) * progress;
            for (int i = 3; i >= 1; i--) {
                float trailProgress = Math.max(0f, progress - i * 0.045f);
                float trailX = from.x + (to.x - from.x) * trailProgress;
                float trailY = from.y + (to.y - from.y) * trailProgress;
                int size = 8 + (4 - i) * 3;
                g.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 30 + i * 15));
                g.fillOval((int) trailX - size / 2, (int) trailY - size / 2, size, size);
            }
            g.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 55));
            g.fillOval((int) x - 22, (int) y - 22, 44, 44);
            g.setColor(CYAN);
            g.fillOval((int) x - 14, (int) y - 14, 28, 28);
            g.setColor(new Color(8, 30, 43));
            centerText(g, String.valueOf(guess), (int) x, (int) y + 5, new Color(8, 30, 43),
                    new Font("SansSerif", Font.BOLD, 13));
        }

        private Point nodeCenter(String id, int w, int h) {
            if (id.equals("start")) return new Point(w / 2, (int) (h * 0.13));
            if (id.equals("if")) return new Point((int) (w * 0.43), (int) (h * 0.37));
            if (id.equals("correct")) return new Point((int) (w * 0.16), (int) (h * 0.78));
            if (id.equals("elseIf")) return new Point((int) (w * 0.78), (int) (h * 0.43));
            if (id.equals("low")) return new Point((int) (w * 0.64), (int) (h * 0.80));
            return new Point((int) (w * 0.87), (int) (h * 0.80));
        }

        private Point[] edgePoints(String from, String to, Point start, Point end) {
            double dx = end.x - start.x;
            double dy = end.y - start.y;
            double length = Math.max(1.0, Math.sqrt(dx * dx + dy * dy));
            double ux = dx / length;
            double uy = dy / length;
            double fromRadius = from.equals("start") ? 35 : from.equals("if") ? 54 : 60;
            double toRadius = to.equals("if") || to.equals("elseIf") ? 48 : 60;
            Point edgeStart = new Point((int) (start.x + ux * fromRadius),
                    (int) (start.y + uy * fromRadius));
            Point edgeEnd = new Point((int) (end.x - ux * toRadius),
                    (int) (end.y - uy * toRadius));
            return new Point[] { edgeStart, edgeEnd };
        }

        private void drawArrowHead(Graphics2D g, Point start, Point end, Color color) {
            double angle = Math.atan2(end.y - start.y, end.x - start.x);
            int size = 9;
            Path2D arrow = new Path2D.Float();
            arrow.moveTo(end.x, end.y);
            arrow.lineTo(end.x - size * Math.cos(angle - Math.PI / 6),
                    end.y - size * Math.sin(angle - Math.PI / 6));
            arrow.lineTo(end.x - size * Math.cos(angle + Math.PI / 6),
                    end.y - size * Math.sin(angle + Math.PI / 6));
            arrow.closePath();
            g.setColor(color);
            g.fill(arrow);
        }

        private void drawPill(Graphics2D g, int centerX, int centerY, String text, Color color) {
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            int width = g.getFontMetrics().stringWidth(text) + 13;
            g.setColor(new Color(18, 28, 48));
            g.fillRoundRect(centerX - width / 2, centerY - 10, width, 19, 10, 10);
            g.setColor(color);
            g.drawRoundRect(centerX - width / 2, centerY - 10, width, 19, 10, 10);
            centerText(g, text, centerX, centerY + 3, color, g.getFont());
        }

        private void centerText(Graphics2D g, String text, int x, int y, Color color, Font font) {
            g.setFont(font);
            g.setColor(color);
            int textWidth = g.getFontMetrics().stringWidth(text);
            g.drawString(text, x - textWidth / 2, y);
        }

        private String edgeKey(String from, String to) {
            return from + "->" + to;
        }
    }
}
