#!/bin/bash
# Script to check all Java files for illegal constructs
# Detailed results are saved to a file, while only summary is shown in console

# Define output file
OUTPUT_FILE="illegal_constructs_report.txt"
# Define timestamp format
TIMESTAMP=$(date "+%Y-%m-%d %H:%M:%S")

# Create a new report file, add title and timestamp
echo "====================================================" > "$OUTPUT_FILE"
echo "       Illegal Constructs Check Report - $TIMESTAMP" >> "$OUTPUT_FILE"
echo "====================================================" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

echo "Starting to check all Java source files..."
echo "Starting to check all Java source files..." >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Counters for tracking the number of problematic files
PROBLEM_FILES=0
TOTAL_FILES=0
PROBLEM_FILE_LIST=""

# Store file list to avoid pipe which creates a subshell
FILE_LIST=$(find src/main/java -name "*.java")

# Process each file without using a pipe
for file in $FILE_LIST; do
    # Show minimal progress in console
    echo -n "."

    # Write detailed information to the file
    echo "Checking file: $file" >> "$OUTPUT_FILE"

    # Run check and output results only to file
    RESULT=$(./mvnw exec:java@strange -Dexec.args="$file" 2>&1)
    echo "$RESULT" >> "$OUTPUT_FILE"

    # Check if results contain warnings
    if echo "$RESULT" | grep -q "WARN"; then
        PROBLEM_FILES=$((PROBLEM_FILES+1))
        # Store problematic file names for summary
        PROBLEM_FILE_LIST="$PROBLEM_FILE_LIST\n- $file"
    fi

    TOTAL_FILES=$((TOTAL_FILES+1))

    printf "\n+++++++++++++++++++++++++++++++++\n" >> "$OUTPUT_FILE"
done

# Add summary information to file
echo "" >> "$OUTPUT_FILE"
echo "Check completed! Examined $TOTAL_FILES files, found problems in $PROBLEM_FILES files." >> "$OUTPUT_FILE"
if [ $PROBLEM_FILES -gt 0 ]; then
    echo -e "Files with problems:$PROBLEM_FILE_LIST" >> "$OUTPUT_FILE"
fi
echo "Check time: $TIMESTAMP" >> "$OUTPUT_FILE"

# Print summary to console
echo ""
echo "Check completed! Examined $TOTAL_FILES files, found problems in $PROBLEM_FILES files."
if [ $PROBLEM_FILES -gt 0 ]; then
    echo -e "Files with problems:$PROBLEM_FILE_LIST"
fi
echo "Detailed results saved to file: $OUTPUT_FILE"
