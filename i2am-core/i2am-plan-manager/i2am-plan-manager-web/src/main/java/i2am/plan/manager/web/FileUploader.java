package i2am.plan.manager.web;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;

@SuppressWarnings("serial") 
public class FileUploader extends HttpServlet {
	//private final String UPLOAD_DIRECTORY = "D://test_files/";
	private final String UPLOAD_DIRECTORY = "/data/test_files/";

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);

		// process only if it is multipart content
		if (isMultipart) {
			// Create a factory for disk-based file items
			FileItemFactory factory = new DiskFileItemFactory();
			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);
			try {
				// Parse the request
				List<FileItem> multiparts = upload.parseRequest(new ServletRequestContext(request));

				String owner = new String();
				String name = new String();
				String path = new String();
				long size = -1;
				String type = new String();
				for (FileItem item : multiparts) {
					if (item.isFormField()) {
						if (item.getFieldName().equals("owner"))
							owner = item.getString();
						else if (item.getFieldName().equals("testName"))
							name = item.getString();
					} else { 
						File directory = new File(UPLOAD_DIRECTORY + owner);
						if ( !directory.exists() )
							directory.mkdir();
						
						path = directory.getPath() + "/" + item.getName();
						type = item.getName().substring(item.getName().lastIndexOf(".")+1);
						
						item.write(new File(path));
						size = new File(path).length();
					}
				}
				DbAdapter.getInstance().addTestData(owner, name, path, size, type);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}
}