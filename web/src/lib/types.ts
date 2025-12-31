export interface FileItem {
    name: string;
    isDirectory: boolean;
    size: number;
}

export interface DirectoryListing {
    path: string;
    files: FileItem[];
}
