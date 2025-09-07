#include <stdio.h>
#include <windows.h>

void launch() {
	FILE* vmoptions_f = fopen(".vmoptions", "r");
	char vmoptions[1024];
	fgets(vmoptions, sizeof(vmoptions), vmoptions_f);
	char command[1024];
	snprintf(command, sizeof(command), "jre\\bin\\java.exe %s org.tonylimps.liberreach.windows.Main\n", vmoptions);
	printf(command); PROCESS_INFORMATION pi = { 0 };
	SetConsoleTitleW(L"LiberReach Console");
	system(command);
}
int main() {
	launch();
	return 0;
}
int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance,
	LPSTR lpCmdLine, int nCmdShow) {
	launch();
	return 0;
}
