import kotlin.math.abs
import kotlin.math.round
import kotlin.math.log2
import kotlin.math.atan2
import kotlin.math.PI
import kotlin.math.pow
import kotlin.random.Random

data class SimulatedData(
    val frequency: Float,
    val note: String,
    val octave: Float,
    val deviation: Float,
    val semitoneDifference: Float,
    val cents: Float
)

object FakeAudioProcessor {

    fun freqToNote(freq: Float): Pair<String, Float> {
        val notes = listOf("A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#")
        // Handle frequencies below the minimum threshold
        if (freq < 27.5 || freq > 6644.88) {
            return Pair("0", 0f) // or return an appropriate default value
        }
        // Calculate the note number based on the frequency
        var noteNumber = 12 * log2(freq / 440) + 49
        noteNumber = round(noteNumber)
        // Determine the note
        val noteIndex = (noteNumber - 1) % notes.size
        val note = notes[noteIndex.toInt()]
        // Determine the octave
        val octave = (noteNumber + 8) / notes.size

        return Pair(note, octave)
    }

    fun generateNoteMap(startOctave: Int, endOctave: Int): Map<Float, Pair<String, Int>> {
        val notes = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val noteMap = mutableMapOf<Float, Pair<String, Int>>()
        val referenceFrequency = 440f // Frequency of A4
        // Calculate frequency for each note in the specified range of octaves
        for (octave in startOctave..endOctave) {
            for (i in notes.indices) {
                // Calculate the note's frequency
                val frequency = referenceFrequency * 2.0.pow((octave - 4) + (i - 9) / 12.0) // Adjust for octave and semitone
                noteMap[frequency.toFloat()] = Pair(notes[i], octave)
            }
        }
        return noteMap
    }

    // Expanded note map for commonly used musical notes within the range
    private val noteMap = generateNoteMap(0, 8)

    fun getSimulatedFrequencyData(): SimulatedData {
        //val frequency = Random.nextFloat() * 900 + 100 // Random frequency between 100–1000 Hz
        val minFrequency = 27.5f // Minimum frequency
        val maxFrequency = 6644.88f  // Maximum frequency
        // Generate a random frequency between minFrequency and maxFrequency
        val frequency = Random.nextFloat() * (maxFrequency - minFrequency) + minFrequency
        //val frequency = 6344.55f // Random frequency between 100–1000 Hz
        // Use freqToNote to get the note and octave
        val (note, octave) = freqToNote(frequency)
        // Determine the closest note in the map for deviation calculation
        val closestNote = noteMap.keys.minByOrNull { abs(it - frequency) } ?: 440f
        // Calculate the deviation from the closest note frequency
        val deviation = frequency - closestNote
        // Calculate the deviation angle in degrees
        // Calculate the semitone difference and cents
        val semitoneDifference = (12 * log2(frequency / closestNote))
        val cents = 1200 * log2(frequency / closestNote)

        return SimulatedData(
            frequency = frequency,
            note = note,
            octave = octave,
            deviation = deviation,
            semitoneDifference = semitoneDifference,
            cents = cents.toFloat()
        )
    }
}
