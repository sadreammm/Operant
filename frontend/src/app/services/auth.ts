import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Tenant {
  id: string;
  name: string;
  ownerEmail: string
  schemaName: string;
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getCurrentTenant(): Observable<Tenant> {
    return this.http.get<Tenant>(`${this.baseUrl}/api/tenant/me`, {
      withCredentials: true
    });
  }

  login(): void {
    window.location.href = `${this.baseUrl}/oauth2/authorization/google`;
  }
}
