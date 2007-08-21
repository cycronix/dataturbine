// JavaEnv.c
// Displays the current path of the JDK or the JRE
//
// Mods:
// 03/01/2002  WHF  Created
//

#include <windows.h>
#include <winreg.h>
#include <stdio.h>

static char buffer[256];

void findJavaHome(HKEY key)
{
	LONG result;
	DWORD type, buffSize;
	HKEY currentVersion;
	
	// Will be changed by call
	buffSize=sizeof(buffer);
	result=RegQueryValueEx(
		key,           // handle to key to query
		"CurrentVersion",  // address of name of value to query
		0,  // reserved
		&type,      // address of buffer for value type
		buffer,       // address of data buffer
		&buffSize);     // address of data buffer size
	if (result!=ERROR_SUCCESS)
	{
		printf("JavaEnv Error: Value \"CurrentVersion\" not found.");
		exit(1);
	}
		
	result=RegOpenKeyEx(
		key,
		buffer,
		(DWORD) 0,
		KEY_READ,
		&currentVersion);
	if (result!=ERROR_SUCCESS)
	{
		printf("JavaEnv Error: Value \"CurrentVersion\"\\%s not found.",
			buffer);
		exit(1);
	}

	buffSize=sizeof(buffer);
	result=RegQueryValueEx(
		currentVersion,           // handle to key to query
		"JavaHome",  // address of name of value to query
		0,  // reserved
		&type,      // address of buffer for value type
		buffer,       // address of data buffer
		&buffSize);     // address of data buffer size
	RegCloseKey(currentVersion);
	if (result!=ERROR_SUCCESS)
	{
		printf("JavaEnv Error: Value \"JavaHome\" not found.");
		exit(1);
	}

}

int main(int argc, char* argv[])
{
	DWORD dwIndex=0, buffSize;
	HKEY software, javasoft, jreOrJdk;	
	LONG result;
	HANDLE jreDir;
	WIN32_FIND_DATA jreDirData;
	char* toFind;
	
	if (argc<3||strcmp(argv[1],"jre")&&strcmp(argv[1],"jdk"))
	{
		printf("JAVAENV: Locates JAVA_HOME, starts Catalina.\n"
			"JAVAENV [jre jdk]  [startup.bat shutdown.bat]\n"
			"Specify whether to use the Java Runtime Environment"
			" or the Java Development\nKit, and which script to "
			"execute.\n");
		exit(1);
	}
	
	if (GetEnvironmentVariable("JAVA_HOME",buffer,sizeof(buffer)))
	{
		printf("JavaEnv: Using predefined JAVA_HOME=\"%s\"\n",
			buffer);
		return system(argv[2]);
	}
	
	jreDir=INVALID_HANDLE_VALUE;
	if (!strcmp(argv[1],"jre"))  // Use a JRE
	{
		// Check to see if we installed a jre with this RBNB:
		jreDir=FindFirstFile(
			"..\\..\\jre",          // pointer to name of the file
  			&jreDirData); 
 		if (jreDir!=INVALID_HANDLE_VALUE)
		{
			strcpy(buffer,"..\\..\\jre");
			FindClose(jreDir);
		}
	}
	if (jreDir==INVALID_HANDLE_VALUE)
	{
		
		result=RegOpenKeyEx(
			HKEY_LOCAL_MACHINE,
			"SOFTWARE",
			(DWORD) 0,
			KEY_READ,
			&software);
			
		if (result!=ERROR_SUCCESS)
		{
			printf("JavaEnv Error: Value \"SOFTWARE\" not found.");
			exit(1);
		}
		
		result=RegOpenKeyEx(
			software,
			"JavaSoft",
			(DWORD) 0,
			KEY_READ,
			&javasoft);
			
		RegCloseKey(software); // no longer needed, should close before possible
					//  exit		
	
		if (result!=ERROR_SUCCESS) 
		{
			printf("JavaEnv Error: Value \"JavaSoft\" not found.");
			exit(1);
		}
			
		if (!strcmp(argv[1],"jdk")) toFind="Java Development Kit";
		else toFind="Java Runtime Environment";
	
		result=RegOpenKeyEx(
			javasoft,
			toFind,
			(DWORD) 0,
			KEY_READ,
			&jreOrJdk);
			
		RegCloseKey(javasoft);
		if (result!=ERROR_SUCCESS) // no JDK, try for JRE
		{
				printf("JavaEnv Error: The key \"%s\" was not found.",
					toFind);
				exit(1);
		}
			
		findJavaHome(jreOrJdk);
	
		RegCloseKey(jreOrJdk);
	} // end if bad handle
	printf("JavaEnv: Setting JAVA_HOME = %s\n",buffer);
	SetEnvironmentVariable("JAVA_HOME",buffer);

	return system(argv[2]);	
}


