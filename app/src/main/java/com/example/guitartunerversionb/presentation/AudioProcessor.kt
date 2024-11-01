import kotlin.math.abs
import kotlin.math.round
import kotlin.math.log2
import kotlin.math.pow
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor

data class Data(
    val frequency: Float,
    val note: String,
    val octave: Float,
    val deviation: Float,
    val semitoneDifference: Float,
    val cents: Float
)

object AudioProcessor {

    private var audioDispatcher: AudioDispatcher? = null

    // This function initializes the audio processing with TarsosDSP
    fun startListening(onFrequencyDetected: (Data) -> Unit) {
        // Sample rate and buffer size for real-time audio processing
        val sampleRate = 44100 // Sample rate
        val bufferSize = 6000 // Buffer size - should be at least 5632
        val overlap = 512 // Overlap size

        // Create an audio dispatcher that captures audio from the microphone
        audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize, overlap)

        // Set up the pitch detection handler
        val pitchDetectionHandler = PitchDetectionHandler { result, _ ->
            val frequency = result.pitch

            if (frequency > 27.5 &&  frequency < 6644.88) { // Filter out erroneous frequencies  freq < 27.5 || freq > 6644.88
                // Convert frequency to note information
                val data = getFrequencyData(frequency)
                onFrequencyDetected(data)
            }
        }

        // Attach pitch processor to dispatcher
        val pitchProcessor = PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.YIN, sampleRate.toFloat(), bufferSize, pitchDetectionHandler)
        audioDispatcher?.addAudioProcessor(pitchProcessor)

        // Start processing audio in a new thread
        Thread {
            audioDispatcher?.run()
        }.start()
    }

    // Function to stop listening
    fun stopListening() {
        audioDispatcher?.stop()
    }

    // Converts a frequency to Data, using your existing methods for note conversion
    private fun getFrequencyData(frequency: Float): Data {
        val (note, octave) = freqToNote(frequency)
        val closestNoteFrequency = noteMap.keys.minByOrNull { abs(it - frequency) } ?: 440f
        val deviation = frequency - closestNoteFrequency
        val semitoneDifference = 12 * log2(frequency / closestNoteFrequency)
        val cents = 1200 * log2(frequency / closestNoteFrequency)

        return Data(
            frequency = frequency,
            note = note,
            octave = octave,
            deviation = deviation,
            semitoneDifference = semitoneDifference,
            cents = cents.toFloat()
        )
    }

    fun freqToNote(freq: Float): Pair<String, Float> {
        val notes = listOf("A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#")
        // Handle frequencies below the minimum threshold
        if (freq < 27.5 || freq > 6644.88) {
            return Pair("", 0f) // or return an appropriate default value
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

    fun getSimulatedFrequencyData(): Data {
        val frequency = 440f // Use a simulated frequency

        val (note, octave) = freqToNote(frequency)
        // Determine the closest note in the map for deviation calculation
        val closestNote = noteMap.keys.minByOrNull { abs(it - frequency) } ?: 440f
        // Calculate the deviation from the closest note frequency
        val deviation = frequency - closestNote // Calculate the deviation angle in degrees
        // Calculate the semitone difference and cents
        val semitoneDifference = (12 * log2(frequency / closestNote))
        val cents = 1200 * log2(frequency / closestNote)

        return Data(
            frequency = frequency,
            note = note,
            octave = octave,
            deviation = deviation,
            semitoneDifference = semitoneDifference,
            cents = cents.toFloat()
        )
    }
}
