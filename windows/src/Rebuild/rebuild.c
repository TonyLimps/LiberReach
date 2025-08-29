#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>

#ifdef _WIN32
#include <direct.h>
#define mkdir(dir) _mkdir(dir)
#define SEP "\\"
#define COPY_CMD "copy /Y "
#define RM_CMD "rmdir /S /Q "
#elif __linux__
#define mkdir(dir) mkdir(dir, 0777)
#define SEP "/"
#define COPY_CMD "cp -r "
#define RM_CMD "rm -rf "
#endif

int main(int argc, char* argv[]) {
	if (argc < 3) {
		printf("<jar-file> <javafx-sdk-path> <launch-program-path> <gcc-path>\n");
		return 1;
	}

	char* jar = argv[1];
	char* javafx = argv[2];
	char* launch = argv[3];
	char* gcc = argv[4];

	char command[512];
	printf("preparing FileRelay directory\n");
	mkdir("FileRelay");
	snprintf(command, sizeof(command), RM_CMD "\"%s\"", "FileRelay");
	system(command);
	mkdir("FileRelay");
	_chdir("FileRelay");

	printf("unpacking jar file\n");
	memset(command, 0, sizeof(command));
	snprintf(command, sizeof(command), "jar xf ..%s%s", SEP, jar);
	system(command);
	printf("done.\n");

	printf("fixing project structure\n");
	memset(command, 0, sizeof(command));
	snprintf(command, sizeof(command), RM_CMD "\"%s\"", "javafx");
	system(command);
	mkdir("libs");
	printf("done.\n");

	printf("copying javafx runtime\n");
	memset(command, 0, sizeof(command));
	snprintf(command, sizeof(command), COPY_CMD "\"%s%slib\" libs", javafx, SEP);
	system(command);
	printf("done.\n");

	printf("analyzing jre dependencies\n");
	memset(command, 0, sizeof(command));
	snprintf(command, sizeof(command), "jdeps --print-module-deps --ignore-missing-deps -recursive ../%s > jdeps.txt", jar);
	system(command);
	char jdeps[1024] = { 0 };
	FILE* jdeps_f = fopen("jdeps.txt", "r");
	fread(jdeps, 1, 1024, jdeps_f);
	printf(jdeps);
	char* newline = strchr(jdeps, '\n');
	*newline = '\0';
	printf("done.\n");

	printf("building java runtime\n");
	memset(command, 0, sizeof(command));
	snprintf(command, sizeof(command), "jlink --add-modules %s --output jre", jdeps);
	system(command);
	printf("done.\n");

	printf("building vmoptions\n");
	FILE* vmoptions = fopen(".vmoptions", "w");
	fprintf(vmoptions, "--add-modules javafx.base,javafx.fxml,javafx.controls,javafx.graphics --module-path ./libs");
	fclose(vmoptions);
	printf("done.\n");

	printf("compiling launch program\n");
	memset(command, 0, sizeof(command));
	snprintf(command, sizeof(command), "%s%sgcc.exe -o FileRelay.exe %s", gcc, SEP, launch);
	system(command);
	printf("all done.\n");
	return 0;
}
