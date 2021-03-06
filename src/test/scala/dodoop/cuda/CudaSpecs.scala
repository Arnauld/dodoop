package dodoop.cuda

import org.specs.Specification
import jcuda.driver._
import jcuda.driver.JCudaDriver._

import java.io.{ByteArrayOutputStream, InputStream, IOException, File}
import jcuda.{LibUtils, Pointer, Sizeof}

class CudaSpecs extends Specification {

  import Misc._
  val baseDir = "/Users/arnauld/Projects/dodoop"
  LibUtils.libDir = baseDir + "/lib"


  "Basic test" should {
    "check for cudaMalloc" in {
      import jcuda.runtime.JCuda
      val pointer = new Pointer
      JCuda.cudaMalloc(pointer, 4)
      println("Pointer: "+pointer)
      JCuda.cudaFree(pointer)
    }
  }

  "Add vectors" should {
    "work" in {
      // Enable exceptions and omit all subsequent error checks
      JCudaDriver.setExceptionsEnabled(true);

      // Create the PTX file by calling the NVCC
      val ptxFileName = preparePtxFile(baseDir + "/data/cuda/" + "JCudaVectorAddKernel.cu");

      // Initialize the driver and create a context for the first device.
      cuInit(0);
      val device = new CUdevice
      cuDeviceGet(device, 0);
      val context = new CUcontext
      cuCtxCreate(context, 0, device);

      // Load the ptx file.
      val module = new CUmodule
      cuModuleLoad(module, ptxFileName);

      // Obtain a function pointer to the "add" function.
      val function = new CUfunction
      cuModuleGetFunction(function, module, "add");

      val numElements = 100000;

      // Allocate and fill the host input data
      val hostInputA = new Array[Float](numElements)
      val hostInputB = new Array[Float](numElements)
      for (i <- 0 until numElements) {
        hostInputA(i) = i.toFloat
        hostInputB(i) = i.toFloat
      }

      // Allocate the device input data, and copy the
      // host input data to the device
      val deviceInputA = new CUdeviceptr
      cuMemAlloc(deviceInputA, numElements * Sizeof.FLOAT);
      cuMemcpyHtoD(deviceInputA, Pointer.to(hostInputA), numElements * Sizeof.FLOAT);

      val deviceInputB = new CUdeviceptr
      cuMemAlloc(deviceInputB, numElements * Sizeof.FLOAT);
      cuMemcpyHtoD(deviceInputB, Pointer.to(hostInputB), numElements * Sizeof.FLOAT);

      // Allocate device output memory
      val deviceOutput = new CUdeviceptr
      cuMemAlloc(deviceOutput, numElements * Sizeof.FLOAT);

      // Set up the kernel parameters: A pointer to an array
      // of pointers which point to the actual values.
      val kernelParameters: Pointer = Pointer.to(
        Pointer.to(Array[Int](numElements)),
        Pointer.to(deviceInputA),
        Pointer.to(deviceInputB),
        Pointer.to(deviceOutput)
      );

      // Call the kernel function.
      val blockSizeX = 256
      val gridSizeX = scala.math.ceil(numElements.toDouble / blockSizeX).toInt
      cuLaunchKernel(function, //
        gridSizeX, 1, 1, // Grid dimension
        blockSizeX, 1, 1, // Block dimension
        0, null, // Shared memory size and stream
        kernelParameters, null // Kernel- and extra parameters
      );
      cuCtxSynchronize()

      // Allocate host output memory and copy the device output
      // to the host.
      val hostOutput = new Array[Float](numElements)
      cuMemcpyDtoH(Pointer.to(hostOutput), deviceOutput, numElements * Sizeof.FLOAT)

      // Verify the result
      var passed = true;
      for (i <- 0 until numElements) {
        val expected = (i + i).toFloat;
        if (scala.math.abs(hostOutput(i) - expected) > 1e-5) {
          println(
            "At index " + i + " found " + hostOutput(i) +
              " but expected " + expected);
          passed = false;
        }
      }

      // Clean up.
      cuMemFree(deviceInputA);
      cuMemFree(deviceInputB);
      cuMemFree(deviceOutput);

      // assert
      passed must_== true
    }
  }
}

object Misc {
  /**
   * The extension of the given file name is replaced with "ptx".
   * If the file with the resulting name does not exist, it is
   * compiled from the given file using NVCC. The name of the
   * PTX file is returned.
   *
   * @param cuFileName The name of the .CU file
   * @return The name of the PTX file
   * @throws IOException If an I/O error occurs
   */
  def preparePtxFile(cuFileName: String): String = {
    var endIndex = cuFileName.lastIndexOf('.')
    if (endIndex == -1) {
      endIndex = cuFileName.length() - 1
    }
    val ptxFileName = cuFileName.substring(0, endIndex + 1) + "ptx"
    val ptxFile = new File(ptxFileName)
    if (ptxFile.exists()) {
      return ptxFileName;
    }

    val cuFile = new File(cuFileName);
    if (!cuFile.exists()) {
      throw new IOException("Input file not found: " + cuFileName)
    }
    val modelString = "-m" + System.getProperty("sun.arch.data.model")
    val command =
      "nvcc " + modelString + " -ptx " + cuFile.getPath + " -o " + ptxFileName

    println("Executing\n" + command);
    val process: Process = Runtime.getRuntime.exec(command);

    val errorMessage = new String(toByteArray(process.getErrorStream))
    val outputMessage = new String(toByteArray(process.getInputStream))
    var exitValue = 0;
    try {
      exitValue = process.waitFor()
    }
    catch {
      case e: InterruptedException =>
        Thread.currentThread().interrupt();
        throw new IOException(
          "Interrupted while waiting for nvcc output", e)
    }

    if (exitValue != 0) {
      println("nvcc process exitValue " + exitValue);
      println("errorMessage:\n" + errorMessage);
      println("outputMessage:\n" + outputMessage);
      throw new IOException(
        "Could not create .ptx file: " + errorMessage);
    }

    println("Finished creating PTX file");
    ptxFileName;
  }

  /**
   * Fully reads the given InputStream and returns it as a byte array
   *
   * @param inputStream The input stream to read
   * @return The byte array containing the data from the input stream
   * @throws IOException If an I/O error occurs
   */
  def toByteArray(inputStream:InputStream):Array[Byte] = {
    val baos = new ByteArrayOutputStream
    val buffer = new Array[Byte](8192)
    var read = 0
    while ({read=inputStream.read(buffer); read != -1}) {
      baos.write(buffer, 0, read);
    }
    baos.toByteArray
  }
}