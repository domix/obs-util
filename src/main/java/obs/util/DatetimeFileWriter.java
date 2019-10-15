package obs.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

public class DatetimeFileWriter implements Callable<Void> {
  private final FileProps props;

  public DatetimeFileWriter(FileProps props) {
    this.props = props;
  }

  @Override
  public Void call() throws Exception {
    System.out.println("Escribiendo Archivo: " + props.getDestination());
    write();
    return null;
  }


  public void write() throws IOException {
    String str = props.getFormat();
    FileOutputStream outputStream = new FileOutputStream(props.getDestination());
    byte[] strToBytes = str.getBytes();
    outputStream.write(strToBytes);
    outputStream.close();
  }
}
