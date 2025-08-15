#include <stdio.h>
#include <windows.h>

int main() {
	FILE* vmoptions_f = fopen(".vmoptions","r");
	char vmoptions[1024];
	fgets(vmoptions, sizeof(vmoptions), vmoptions_f);
	char command[1024];
	snprintf(command, sizeof(command), "jre\\bin\\java.exe %s com.tonylimps.filerelay.windows.Main\n", vmoptions);
	printf(command); PROCESS_INFORMATION pi = { 0 };
	SetConsoleTitleW(L"FileRelay Console");
	system(command);
	return 0;
}
